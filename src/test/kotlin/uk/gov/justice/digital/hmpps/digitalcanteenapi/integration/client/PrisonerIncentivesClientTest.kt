package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerincentivesclient.PrisonerIncentivesClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_NUMBER
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

    with(result!!) {
      assertThat(id).isEqualTo(12345L)
      assertThat(iepCode).isEqualTo("STD")
      assertThat(iepLevel).isEqualTo("Standard")
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(iepDate).isEqualTo("2025-01-15")
      assertThat(iepTime).isEqualTo("14:30:00")
    }
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
