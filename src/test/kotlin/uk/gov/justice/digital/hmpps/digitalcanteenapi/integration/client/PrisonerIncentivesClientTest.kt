package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerIncentivesClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.PrisonerIncentivesMockServer

class PrisonerIncentivesClientTest {
  private lateinit var client: PrisonerIncentivesClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    client = PrisonerIncentivesClient(webClient)
  }

  @Test
  fun `getPrisoner - successfully returns prisoner`() {
    server.stubGetPrisoner()

    val result = client.getPrisoner(PRISONER_NUMBER).block()

    assertThat(result).isNotNull
    assertThat(result!!.id).isEqualTo(12345L)
    assertThat(result.iepCode).isEqualTo("STD")
    assertThat(result.iepLevel).isEqualTo("standard")
    assertThat(result.prisonerNumber).isEqualTo(PRISONER_NUMBER)
    assertThat(result.bookingId).isEqualTo(12345L)
    assertThat(result.iepDate).isEqualTo("aDate")
    assertThat(result.iepTime).isEqualTo("aTime")
    assertThat(result.iepDetails[0].id).isEqualTo(12345L)
  }

  companion object {
    @JvmField
    internal val server = PrisonerIncentivesMockServer()

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