package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaAdminClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.MEDUSA_ADMIN_EMAIL
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.MEDUSA_ADMIN_PASSWORD
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.MedusaMockServer

class MedusaAdminClientTest {
  private lateinit var client: MedusaAdminClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    client = MedusaAdminClient(webClient, MEDUSA_ADMIN_EMAIL, MEDUSA_ADMIN_PASSWORD)
  }

  @Test
  fun `getAdminToken - successfully returns admin token`() {
    server.stubGetAdminToken()

    val result = client.getAdminToken().block()

    assertThat(result).isNotNull
    assertThat(result).isEqualTo("test-token")
  }

  @Test
  fun `getMedusaAdminTest- successfully returns admin route request`() {
    server.stubGetMedusaAdminTest()

    val result = client.medusaAdminTest().block()

    assertThat(result).isNotNull
    with(result!!) {
      assertThat(status).isEqualTo("successful call to medusa admin")
    }
  }

  companion object {
    @JvmField
    internal val server = MedusaMockServer()

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
