package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient.PrisonerAdjudicationsClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PinPhoneTestFixture
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment.PinPhonePrisonerEnrichmentService

@ExtendWith(MockitoExtension::class)
class PinPhonePrisonerEnrichmentServiceTest {

  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @Mock
  lateinit var prisonerAdjudicationsClient: PrisonerAdjudicationsClient

  @Mock
  lateinit var btPinPhoneClient: BtPinPhoneClient

  @Mock
  lateinit var prisonFinanceClient: PrisonFinanceClient

  private lateinit var service: PinPhonePrisonerEnrichmentService

  @BeforeEach
  fun beforeEach() {
    service = PinPhonePrisonerEnrichmentService(
      prisonerSearchClient,
      prisonerAdjudicationsClient,
      btPinPhoneClient,
      prisonFinanceClient,
    )
  }

  @Test
  fun `getEnrichedPrisoner - returns fully enriched prisoner and has adjudications`() {
    val prisoner = PinPhoneTestFixture.Prisoner()
    val prisonerResponse = PinPhoneTestFixture.prisonerSearchResponseDto()
    val incentivesResponse = PinPhoneTestFixture.prisonerSearchIncentiveResponseDto()
    val adjudications = PinPhoneTestFixture.activePunishments()
    val prisonerBalance = PinPhoneTestFixture.balanceDto()
    val prisonerBalanceResponse = PinPhoneTestFixture.balanceResponseDto()
    val btPinPhoneBalance = PinPhoneTestFixture.btPinPhoneDto()
    val btPinPhoneResponse = PinPhoneTestFixture.btPinPhoneResponseDto()

    whenever(prisonerSearchClient.getPrisoner(PinPhoneTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))
    whenever(btPinPhoneClient.getPrisonerBalance(any()))
      .thenReturn(Mono.just(btPinPhoneBalance))
    whenever(prisonFinanceClient.getPrisonerBalance(PinPhoneTestFixture.BOOKING_ID))
      .thenReturn(Mono.just(prisonerBalance))
    whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(PinPhoneTestFixture.BOOKING_ID))
      .thenReturn(Mono.just(adjudications))

    val result = service.getEnrichedPrisoner(PinPhoneTestFixture.PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        // prisoner search
        Assertions.assertThat(enriched.prisoner).isEqualTo(prisonerResponse)
        // incentives
        Assertions.assertThat(enriched.incentives).isEqualTo(incentivesResponse)
        // bt pin phone balance
        Assertions.assertThat(enriched.prisonerBtBalance).isEqualTo(btPinPhoneResponse)
        // prisoner finance balance
        Assertions.assertThat(enriched.prisonerBalance).isEqualTo(prisonerBalanceResponse)
        // adjudications
        Assertions.assertThat(enriched.hasActiveAdjudications).isTrue()
        Assertions.assertThat(enriched.activeAdjudications).hasSize(1)
        Assertions.assertThat(enriched.activeAdjudications?.map { it.privilegeType }).containsExactly("CANTEEN")
      }
      .verifyComplete()
  }

  @Test
  fun `getEnrichedPrisoner - returns fully enriched prisoner does not have adjudications`() {
    val prisoner = PinPhoneTestFixture.Prisoner()
    val prisonerResponse = PinPhoneTestFixture.prisonerSearchResponseDto()
    val incentivesResponse = PinPhoneTestFixture.prisonerSearchIncentiveResponseDto()
    val prisonerBalance = PinPhoneTestFixture.balanceDto()
    val prisonerBalanceResponse = PinPhoneTestFixture.balanceResponseDto()
    val btPinPhoneBalance = PinPhoneTestFixture.btPinPhoneDto()
    val btPinPhoneResponse = PinPhoneTestFixture.btPinPhoneResponseDto()

    whenever(prisonerSearchClient.getPrisoner(PinPhoneTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))
    whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(PinPhoneTestFixture.BOOKING_ID))
      .thenReturn(Mono.empty())
    whenever(btPinPhoneClient.getPrisonerBalance(any()))
      .thenReturn(Mono.just(btPinPhoneBalance))
    whenever(prisonFinanceClient.getPrisonerBalance(PinPhoneTestFixture.BOOKING_ID))
      .thenReturn(Mono.just(prisonerBalance))

    val result = service.getEnrichedPrisoner(PinPhoneTestFixture.PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        // prisoner search
        Assertions.assertThat(enriched.prisoner).isEqualTo(prisonerResponse)
        // incentives
        Assertions.assertThat(enriched.incentives).isEqualTo(incentivesResponse)
        // bt pin phone balance
        Assertions.assertThat(enriched.prisonerBtBalance).isEqualTo(btPinPhoneResponse)
        // prisoner finance balance
        Assertions.assertThat(enriched.prisonerBalance).isEqualTo(prisonerBalanceResponse)
        // adjudications
        Assertions.assertThat(enriched.hasActiveAdjudications).isFalse()
        Assertions.assertThat(enriched.activeAdjudications).isNull()
      }
      .verifyComplete()
  }

  @Test
  fun `getEnrichedPrisoner - returns partial enriched prisoner can't find booking`() {
    val prisoner = PinPhoneTestFixture.Prisoner()
    val prisonerResponse = PinPhoneTestFixture.prisonerSearchResponseDto()
    val incentivesResponse = PinPhoneTestFixture.prisonerSearchIncentiveResponseDto()
    val btPinPhoneBalance = PinPhoneTestFixture.btPinPhoneDto()
    val btPinPhoneResponse = PinPhoneTestFixture.btPinPhoneResponseDto()

    whenever(prisonerSearchClient.getPrisoner(PinPhoneTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))
    whenever(btPinPhoneClient.getPrisonerBalance(any()))
      .thenReturn(Mono.just(btPinPhoneBalance))

    val result = service.getEnrichedPrisoner(PinPhoneTestFixture.PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        // prisoner search
        Assertions.assertThat(enriched.prisoner).isEqualTo(prisonerResponse)
        // incentives
        Assertions.assertThat(enriched.incentives).isEqualTo(incentivesResponse)
        // bt pin phone balance
        Assertions.assertThat(enriched.prisonerBtBalance).isEqualTo(btPinPhoneResponse)
        // prisoner finance balance
        Assertions.assertThat(enriched.prisonerBalance).isNull()
        // adjudications
        Assertions.assertThat(enriched.hasActiveAdjudications).isFalse()
        Assertions.assertThat(enriched.activeAdjudications).isNull()
      }
      .verifyComplete()
  }

  @Suppress("LongMethod")
  @ParameterizedTest
  @CsvSource(
    "adjudications-failure",
    "bt-failure",
    "finance-failure",
  )
  fun `getEnrichedPrisoner - handles service errors`(failingService: String) {
    val prisoner = PinPhoneTestFixture.Prisoner()
    val prisonerResponse = PinPhoneTestFixture.prisonerSearchResponseDto()
    val incentivesResponse = PinPhoneTestFixture.prisonerSearchIncentiveResponseDto()
    val prisonerBalance = PinPhoneTestFixture.balanceDto()
    val btPinPhoneBalance = PinPhoneTestFixture.btPinPhoneDto()

    whenever(prisonerSearchClient.getPrisoner(PinPhoneTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))

    when (failingService) {
      "adjudications-failure" -> {
        whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(PinPhoneTestFixture.BOOKING_ID))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
        whenever(prisonFinanceClient.getPrisonerBalance(PinPhoneTestFixture.BOOKING_ID))
          .thenReturn(Mono.just(prisonerBalance))
        whenever(btPinPhoneClient.getPrisonerBalance(any()))
          .thenReturn(Mono.just(btPinPhoneBalance))
      }

      "bt-failure" -> {
        whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(PinPhoneTestFixture.BOOKING_ID))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
        whenever(prisonFinanceClient.getPrisonerBalance(PinPhoneTestFixture.BOOKING_ID))
          .thenReturn(Mono.just(prisonerBalance))
        whenever(btPinPhoneClient.getPrisonerBalance(any()))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
      }

      "finance-failure" -> {
        whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(PinPhoneTestFixture.BOOKING_ID))
          .thenReturn(Mono.empty())
        whenever(prisonFinanceClient.getPrisonerBalance(PinPhoneTestFixture.BOOKING_ID))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
        whenever(btPinPhoneClient.getPrisonerBalance(any()))
          .thenReturn(Mono.just(btPinPhoneBalance))
      }
    }

    val result = service.getEnrichedPrisoner(PinPhoneTestFixture.PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        Assertions.assertThat(enriched.prisoner).isEqualTo(prisonerResponse)
        Assertions.assertThat(enriched.incentives).isEqualTo(incentivesResponse)

        when (failingService) {
          "adjudications-failure" -> {
            Assertions.assertThat(enriched.hasActiveAdjudications).isFalse()
          }

          "bt-failure" -> {
            Assertions.assertThat(enriched.prisonerBtBalance).isNull()
          }

          "finance-failure" -> {
            Assertions.assertThat(enriched.prisonerBalance).isNull()
          }
        }
      }
      .verifyComplete()
  }
}
