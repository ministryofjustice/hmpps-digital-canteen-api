package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneBalanceRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.BT_CLIENT_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.BT_CLIENT_SECRET
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.BtMockServer

class BtPinPhoneClientTest {
  private lateinit var client: BtPinPhoneClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    client = BtPinPhoneClient(webClient, BT_CLIENT_ID, BT_CLIENT_SECRET)
  }

  @Test
  fun `getBearerToken - successfully returns admin token`() {
    server.stubGetBtAuthToken()

    val result = client.getBtToken().block()

    assertThat(result).isNotNull
    assertThat(result?.accessToken).isEqualTo("bt-test-token")
    assertThat(result?.tokenType).isEqualTo("Bearer")
    assertThat(result?.expiresIn).isEqualTo(3600)
  }

  @Test
  fun `getBtBalance successfully BT balance and credit limit`() {
    server.stubBtGetBalance()

    val result = client.getPrisonerBalance(BtPinPhoneBalanceRequest("testReference", "xyz")).block()

    assertThat(result).isNotNull
    assertThat(result?.reference).isEqualTo("testReference")
    assertThat(result?.prisonerId).isEqualTo("xyz")
    assertThat(result?.balancePence).isEqualTo(200001)
    assertThat(result?.creditLimitPence).isEqualTo(500000)
  }

  companion object {
    @JvmField
    internal val server = BtMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      server.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      server.stop()
    }
  }
}
