package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.TestFixture
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment.PinPhonePrisonerEnrichmentService

@ExtendWith(MockitoExtension::class)
class PinPhonePrisonerEnrichmentServiceTest {

  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @Mock
  lateinit var prisonFinanceClient: PrisonFinanceClient

  private lateinit var service: PinPhonePrisonerEnrichmentService

  @BeforeEach
  fun beforeEach() {
    service = PinPhonePrisonerEnrichmentService(
      prisonerSearchClient,
      prisonFinanceClient,
    )
  }

  @Test
  fun `getEnrichedPrisoner - returns fully enriched prisoner and has adjudications`() {
    val prisoner = TestFixture.Prisoner()
    val prisonerResponse = TestFixture.prisonerSearchResponseDto()
    val prisonerBalance = TestFixture.balanceDto()
    val prisonerBalanceResponse = TestFixture.balanceResponseDto()

    whenever(prisonerSearchClient.getPrisoner(TestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))
    whenever(prisonFinanceClient.getPrisonerBalance(TestFixture.BOOKING_ID))
      .thenReturn(Mono.just(prisonerBalance))

    val result = service.getEnrichedPrisoner(TestFixture.PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        // prisoner search
        Assertions.assertThat(enriched.prisoner).isEqualTo(prisonerResponse)
        // prisoner finance balance
        Assertions.assertThat(enriched.prisonerBalance).isEqualTo(prisonerBalanceResponse)
      }
      .verifyComplete()
  }

  @ParameterizedTest
  @CsvSource(
    "prisoner-search",
    "finance-failure",
  )
  fun `getEnrichedPrisoner - handles service errors`(failingService: String) {
    val prisoner = TestFixture.Prisoner()

    when (failingService) {
      "prisoner-search" -> {
        whenever(prisonerSearchClient.getPrisoner(TestFixture.PRISONER_NUMBER))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
      }
      "finance-failure" -> {
        whenever(prisonerSearchClient.getPrisoner(TestFixture.PRISONER_NUMBER))
          .thenReturn(Mono.just(prisoner))
        whenever(prisonFinanceClient.getPrisonerBalance(TestFixture.BOOKING_ID))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
      }
    }

    val result = service.getEnrichedPrisoner(TestFixture.PRISONER_NUMBER)

    StepVerifier.create(result)
      .verifyErrorMessage("Service unavailable")
  }
}
