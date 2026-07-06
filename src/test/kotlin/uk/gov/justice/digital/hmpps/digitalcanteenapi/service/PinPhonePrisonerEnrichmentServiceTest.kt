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
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient.PrisonerAdjudicationsClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerincentivesclient.PrisonerIncentivesClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PrisonerEnrichmentTestFixture

@ExtendWith(MockitoExtension::class)
class PinPhonePrisonerEnrichmentServiceTest {

  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @Mock
  lateinit var prisonerAdjudicationsClient: PrisonerAdjudicationsClient

  @Mock
  lateinit var prisonerIncentivesClient: PrisonerIncentivesClient

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
      prisonerIncentivesClient,
      btPinPhoneClient,
      prisonFinanceClient,
    )
  }

  @Test
  fun `getEnrichedPrisoner - returns fully enriched prisoner and has adjudications`() {
    val prisoner = PrisonerEnrichmentTestFixture.prisonerSearchDto()
    val incentives = PrisonerEnrichmentTestFixture.prisonerIncentivesDto()
    val adjudications = PrisonerEnrichmentTestFixture.activePunishments()
    val prisonerBalance = PrisonerEnrichmentTestFixture.balanceDto()
    val btPinPhoneBalance = PrisonerEnrichmentTestFixture.btPinPhoneDto()

    whenever(prisonerSearchClient.getPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))
    whenever(prisonerIncentivesClient.getPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(incentives))
    whenever(btPinPhoneClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(btPinPhoneBalance))
    whenever(prisonFinanceClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.BOOKING_ID))
      .thenReturn(Mono.just(prisonerBalance))
    whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(PrisonerEnrichmentTestFixture.BOOKING_ID))
      .thenReturn(Mono.just(adjudications))

    val result = service.getEnrichedPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        // prisoner search
        Assertions.assertThat(enriched.prisoner).isEqualTo(prisoner)
        // incentives
        Assertions.assertThat(enriched.incentives).isEqualTo(incentives)
        // bt pin phone balance
        Assertions.assertThat(enriched.prisonerBtBalance).isEqualTo(btPinPhoneBalance)
        // prisoner finance balance
        Assertions.assertThat(enriched.prisonerBalance).isEqualTo(prisonerBalance)
        // adjudications
        Assertions.assertThat(enriched.hasActiveAdjudications).isTrue()
        Assertions.assertThat(enriched.activeAdjudications).hasSize(1)
        Assertions.assertThat(enriched.activeAdjudications?.map { it.privilegeType }).containsExactly("CANTEEN")
      }
      .verifyComplete()
  }

  @Test
  fun `getEnrichedPrisoner - returns fully enriched prisoner does not have adjudications`() {
    val prisoner = PrisonerEnrichmentTestFixture.prisonerSearchDto()
    val incentives = PrisonerEnrichmentTestFixture.prisonerIncentivesDto()
    val prisonerBalance = PrisonerEnrichmentTestFixture.balanceDto()
    val btPinPhoneBalance = PrisonerEnrichmentTestFixture.btPinPhoneDto()

    whenever(prisonerSearchClient.getPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))
    whenever(prisonerIncentivesClient.getPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(incentives))
    whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(PrisonerEnrichmentTestFixture.BOOKING_ID))
      .thenReturn(Mono.empty())
    whenever(btPinPhoneClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(btPinPhoneBalance))
    whenever(prisonFinanceClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.BOOKING_ID))
      .thenReturn(Mono.just(prisonerBalance))

    val result = service.getEnrichedPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        // prisoner search
        Assertions.assertThat(enriched.prisoner).isEqualTo(prisoner)
        // incentives
        Assertions.assertThat(enriched.incentives).isEqualTo(incentives)
        // bt pin phone balance
        Assertions.assertThat(enriched.prisonerBtBalance).isEqualTo(btPinPhoneBalance)
        // prisoner finance balance
        Assertions.assertThat(enriched.prisonerBalance).isEqualTo(prisonerBalance)
        // adjudications
        Assertions.assertThat(enriched.hasActiveAdjudications).isFalse()
        Assertions.assertThat(enriched.activeAdjudications).isNull()
      }
      .verifyComplete()
  }

  @Test
  fun `getEnrichedPrisoner - returns partial enriched prisoner can't find booking`() {
    val prisoner = PrisonerEnrichmentTestFixture.prisonerSearchDto().copy(bookingId = null)
    val incentives = PrisonerEnrichmentTestFixture.prisonerIncentivesDto()
    val btPinPhoneBalance = PrisonerEnrichmentTestFixture.btPinPhoneDto()

    whenever(prisonerSearchClient.getPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))
    whenever(prisonerIncentivesClient.getPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(incentives))
    whenever(btPinPhoneClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(btPinPhoneBalance))

    val result = service.getEnrichedPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        // prisoner search
        Assertions.assertThat(enriched.prisoner).isEqualTo(prisoner)
        // incentives
        Assertions.assertThat(enriched.incentives).isEqualTo(incentives)
        // bt pin phone balance
        Assertions.assertThat(enriched.prisonerBtBalance).isEqualTo(btPinPhoneBalance)
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
    "incentives-failure",
    "adjudications-failure",
    "bt-failure",
    "finance-failure",
  )
  fun `getEnrichedPrisoner - handles service errors`(failingService: String) {
    val prisoner = PrisonerEnrichmentTestFixture.prisonerSearchDto()
    val incentives = PrisonerEnrichmentTestFixture.prisonerIncentivesDto()
    val prisonerBalance = PrisonerEnrichmentTestFixture.balanceDto()
    val btPinPhoneBalance = PrisonerEnrichmentTestFixture.btPinPhoneDto()

    whenever(prisonerSearchClient.getPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))

    when (failingService) {
      "incentives-failure" -> {
        whenever(prisonerIncentivesClient.getPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
        whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(PrisonerEnrichmentTestFixture.BOOKING_ID))
          .thenReturn(Mono.empty())
        whenever(prisonFinanceClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.BOOKING_ID))
          .thenReturn(Mono.just(prisonerBalance))
        whenever(btPinPhoneClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
          .thenReturn(Mono.just(btPinPhoneBalance))
      }

      "adjudications-failure" -> {
        whenever(prisonerIncentivesClient.getPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
          .thenReturn(Mono.just(incentives))
        whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(PrisonerEnrichmentTestFixture.BOOKING_ID))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
        whenever(prisonFinanceClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.BOOKING_ID))
          .thenReturn(Mono.just(prisonerBalance))
        whenever(btPinPhoneClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
          .thenReturn(Mono.just(btPinPhoneBalance))
      }

      "bt-failure" -> {
        whenever(prisonerIncentivesClient.getPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
          .thenReturn(Mono.just(incentives))
        whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(PrisonerEnrichmentTestFixture.BOOKING_ID))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
        whenever(prisonFinanceClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.BOOKING_ID))
          .thenReturn(Mono.just(prisonerBalance))
        whenever(btPinPhoneClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
      }

      "finance-failure" -> {
        whenever(prisonerIncentivesClient.getPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
          .thenReturn(Mono.just(incentives))
        whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(PrisonerEnrichmentTestFixture.BOOKING_ID))
          .thenReturn(Mono.empty())
        whenever(prisonFinanceClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.BOOKING_ID))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
        whenever(btPinPhoneClient.getPrisonerBalance(PrisonerEnrichmentTestFixture.PRISONER_NUMBER))
          .thenReturn(Mono.just(btPinPhoneBalance))
      }
    }

    val result = service.getEnrichedPrisoner(PrisonerEnrichmentTestFixture.PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        Assertions.assertThat(enriched.prisoner).isEqualTo(prisoner)

        when (failingService) {
          "incentives-failure" -> {
            Assertions.assertThat(enriched.incentives).isNull()
          }

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
