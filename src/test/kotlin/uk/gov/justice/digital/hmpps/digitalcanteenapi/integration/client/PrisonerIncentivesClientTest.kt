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

    with(result!!) {
      assertThat(id).isEqualTo(12345L)
      assertThat(iepCode).isEqualTo("STD")
      assertThat(iepLevel).isEqualTo("Standard")
      assertThat(prisonerNumber).isEqualTo(PRISONER_NUMBER)
      assertThat(bookingId).isEqualTo(123456L)
      assertThat(iepDate).isEqualTo("2025-01-15")
      assertThat(iepTime).isEqualTo("14:30:00")
      assertThat(nextReviewDate).isEqualTo("2025-07-15")
      assertThat(daysSinceReview).isEqualTo(30)
    }
    assertThat(result.iepDetails).hasSize(1)
    with(result.iepDetails[0]) {
      assertThat(id).isEqualTo(12345L)
      assertThat(iepLevel).isEqualTo("Standard")
      assertThat(iepCode).isEqualTo("STD")
      assertThat(comments).isEqualTo("something")
      assertThat(isRealReview).isTrue()
      assertThat(reviewType).isEqualTo("REVIEW")
      assertThat(agencyId).isEqualTo("test")
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
