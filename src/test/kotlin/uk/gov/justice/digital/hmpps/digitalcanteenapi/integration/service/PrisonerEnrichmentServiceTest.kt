package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.service

import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerAdjudicationsClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerHealthAndMedicationClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerIncentivesClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PrisonerEnrichmentTestFixture
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PrisonerEnrichmentTestFixture.BOOKING_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PrisonerEnrichmentTestFixture.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.PrisonerEnrichmentService

@ExtendWith(MockitoExtension::class)
class PrisonerEnrichmentServiceTest {

  @Mock
  lateinit var prisonerHealthAndMedicationClient: PrisonerHealthAndMedicationClient

  @Mock
  lateinit var prisonerSearchClient: PrisonerSearchClient

  @Mock
  lateinit var prisonerAdjudicationsClient: PrisonerAdjudicationsClient

  @Mock
  lateinit var prisonerIncentivesClient: PrisonerIncentivesClient

  private lateinit var service: PrisonerEnrichmentService

  @BeforeEach
  fun beforeEach() {
    service = PrisonerEnrichmentService(
      prisonerHealthAndMedicationClient,
      prisonerSearchClient,
      prisonerAdjudicationsClient,
      prisonerIncentivesClient,
    )
  }

  @Test
  fun `getEnrichedPrisoner - returns fully enriched prisoner and has adjudications`() {
    val prisoner = PrisonerEnrichmentTestFixture.prisonerSearchDto()
    val health = PrisonerEnrichmentTestFixture.healthAndMedicationDto()
    val incentives = PrisonerEnrichmentTestFixture.prisonerIncentivesDto()
    val adjudications = PrisonerEnrichmentTestFixture.activePunishments()

    whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))
    whenever(prisonerHealthAndMedicationClient.getPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(health))
    whenever(prisonerIncentivesClient.getPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(incentives))
    whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(BOOKING_ID))
      .thenReturn(Mono.just(adjudications))

    val result = service.getEnrichedPrisoner(PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        // prisoner search
        assertThat(enriched.prisoner).isEqualTo(prisoner)
        // health and medication
        assertThat(enriched.foodAllergies).containsExactly("FOOD_ALLERGY_NUTS")
        assertThat(enriched.medicalDietaryRequirements).containsExactly("DIET_GLUTEN_FREE")
        assertThat(enriched.personalisedDietaryRequirements).containsExactly("DIET_HALAL")
        assertThat(enriched.cateringInstructions).isEqualTo("Cake")
        // incentives
        assertThat(enriched.incentives).isEqualTo(incentives)
        // adjudications
        assertThat(enriched.hasActiveAdjudications).isTrue()
        assertThat(enriched.activeAdjudications).hasSize(1)
        assertThat(enriched.activeAdjudications?.map { it.privilegeType }).containsExactly("CANTEEN")
      }
      .verifyComplete()
  }

  @Test
  fun `getEnrichedPrisoner - returns fully enriched prisoner does not have adjudications`() {
    val prisoner = PrisonerEnrichmentTestFixture.prisonerSearchDto()
    val health = PrisonerEnrichmentTestFixture.healthAndMedicationDto()
    val incentives = PrisonerEnrichmentTestFixture.prisonerIncentivesDto()

    whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))
    whenever(prisonerHealthAndMedicationClient.getPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(health))
    whenever(prisonerIncentivesClient.getPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(incentives))
    whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(BOOKING_ID))
      .thenReturn(Mono.empty())

    val result = service.getEnrichedPrisoner(PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        // prisoner search
        assertThat(enriched.prisoner).isEqualTo(prisoner)
        // health and medication
        assertThat(enriched.foodAllergies).containsExactly("FOOD_ALLERGY_NUTS")
        assertThat(enriched.medicalDietaryRequirements).containsExactly("DIET_GLUTEN_FREE")
        assertThat(enriched.personalisedDietaryRequirements).containsExactly("DIET_HALAL")
        assertThat(enriched.cateringInstructions).isEqualTo("Cake")
        // incentives
        assertThat(enriched.incentives).isEqualTo(incentives)
        // adjudications
        assertThat(enriched.hasActiveAdjudications).isFalse()
        assertThat(enriched.activeAdjudications).isNull()
      }
      .verifyComplete()
  }

  @Test
  fun `getEnrichedPrisoner - returns partial enriched prisoner can't find booking`() {
    val prisoner = PrisonerEnrichmentTestFixture.prisonerSearchDto().copy(bookingId = null)
    val health = PrisonerEnrichmentTestFixture.healthAndMedicationDto()
    val incentives = PrisonerEnrichmentTestFixture.prisonerIncentivesDto()

    whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))
    whenever(prisonerHealthAndMedicationClient.getPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(health))
    whenever(prisonerIncentivesClient.getPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(incentives))

    val result = service.getEnrichedPrisoner(PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        // prisoner search
        assertThat(enriched.prisoner).isEqualTo(prisoner)
        // health and medication
        assertThat(enriched.foodAllergies).containsExactly("FOOD_ALLERGY_NUTS")
        assertThat(enriched.medicalDietaryRequirements).containsExactly("DIET_GLUTEN_FREE")
        assertThat(enriched.personalisedDietaryRequirements).containsExactly("DIET_HALAL")
        assertThat(enriched.cateringInstructions).isEqualTo("Cake")
        // incentives
        assertThat(enriched.incentives).isEqualTo(incentives)
        // adjudications
        assertThat(enriched.hasActiveAdjudications).isFalse()
        assertThat(enriched.activeAdjudications).isNull()
      }
      .verifyComplete()
  }

  @ParameterizedTest
  @CsvSource(
    "health-failure",
    "incentives-failure",
    "adjudications-failure",
  )
  fun `getEnrichedPrisoner - handles service errors`(failingService: String) {
    val prisoner = PrisonerEnrichmentTestFixture.prisonerSearchDto()
    val health = PrisonerEnrichmentTestFixture.healthAndMedicationDto()
    val incentives = PrisonerEnrichmentTestFixture.prisonerIncentivesDto()

    whenever(prisonerSearchClient.getPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(prisoner))

    when (failingService) {
      "health-failure" -> {
        whenever(prisonerHealthAndMedicationClient.getPrisoner(PRISONER_NUMBER))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
        whenever(prisonerIncentivesClient.getPrisoner(PRISONER_NUMBER))
          .thenReturn(Mono.just(incentives))
        whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(BOOKING_ID))
          .thenReturn(Mono.empty())
      }

      "incentives-failure" -> {
        whenever(prisonerHealthAndMedicationClient.getPrisoner(PRISONER_NUMBER))
          .thenReturn(Mono.just(health))
        whenever(prisonerIncentivesClient.getPrisoner(PRISONER_NUMBER))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
        whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(BOOKING_ID))
          .thenReturn(Mono.empty())
      }

      "adjudications-failure" -> {
        whenever(prisonerHealthAndMedicationClient.getPrisoner(PRISONER_NUMBER))
          .thenReturn(Mono.just(health))
        whenever(prisonerIncentivesClient.getPrisoner(PRISONER_NUMBER))
          .thenReturn(Mono.just(incentives))
        whenever(prisonerAdjudicationsClient.getPrisonerAdjudication(BOOKING_ID))
          .thenReturn(Mono.error(RuntimeException("Service unavailable")))
      }
    }

    val result = service.getEnrichedPrisoner(PRISONER_NUMBER)

    StepVerifier.create(result)
      .assertNext { enriched ->
        assertThat(enriched.prisoner).isEqualTo(prisoner)

        when (failingService) {
          "health-failure" -> {
            assertThat(enriched.foodAllergies).isNull()
            assertThat(enriched.incentives).isNotNull
          }

          "incentives-failure" -> {
            assertThat(enriched.foodAllergies).isNotNull
            assertThat(enriched.incentives).isNull()
          }

          "adjudications-failure" -> {
            assertThat(enriched.hasActiveAdjudications).isFalse()
            assertThat(enriched.foodAllergies).isNotNull
          }
        }
      }
      .verifyComplete()
  }
}
