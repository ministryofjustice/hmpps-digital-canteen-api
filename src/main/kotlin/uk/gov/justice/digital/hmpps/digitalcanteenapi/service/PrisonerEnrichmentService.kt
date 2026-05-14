package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerAdjudicationsClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerHealthAndMedicationClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerIncentivesClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.PrisonerIncentivesDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.PrisonerSearchDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.Punishment
import java.util.*

@Component
class PrisonerEnrichmentService(
  private val prisonerHealthAndMedicationClient: PrisonerHealthAndMedicationClient,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonerAdjudicationsClient: PrisonerAdjudicationsClient,
  private val prisonerIncentivesClient: PrisonerIncentivesClient,
) {

  fun getEnrichedPrisoner(prisonerNumber: String): Mono<EnrichedPrisonerDto> {
    val prisonerMono =
      prisonerSearchClient.getPrisoner(prisonerNumber).cache()

    val healthMono =
      prisonerHealthAndMedicationClient.getPrisoner(prisonerNumber)
        .onErrorResume { Mono.empty() }

    val incentivesMono =
      prisonerIncentivesClient.getPrisoner(prisonerNumber)
        .onErrorResume { Mono.empty() }

    val activeAdjudicationsMono = prisonerMono
      .flatMap { prisoner ->
        prisoner.bookingId?.let { bookingId ->
          prisonerAdjudicationsClient.getPrisonerAdjudication(bookingId)
        } ?: Mono.empty()
      }
      .onErrorResume { Mono.empty() }

    return Mono.zip(
      prisonerMono,
      healthMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
      incentivesMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
      activeAdjudicationsMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
    )
      .map { tuple ->
        val healthAndMedication = tuple.t2.orElse(null)
        val dietAndAllergy = healthAndMedication?.dietAndAllergy
        val activeAdjudications = tuple.t4.orElse(null)
        EnrichedPrisonerDto(
          prisoner = tuple.t1,
          foodAllergies = dietAndAllergy?.foodAllergies?.value?.map { it.value.id },
          medicalDietaryRequirements = dietAndAllergy?.medicalDietaryRequirements?.value?.map { it.value.id },
          personalisedDietaryRequirements = dietAndAllergy?.personalisedDietaryRequirements?.value?.map { it.value.id },
          cateringInstructions = dietAndAllergy?.cateringInstructions?.value,
          incentives = tuple.t3.orElse(null),
          hasActiveAdjudications = !activeAdjudications.isNullOrEmpty(),
          activeAdjudications = activeAdjudications?.takeIf { it.isNotEmpty() },
        )
      }
  }

  data class EnrichedPrisonerDto(
    val prisoner: PrisonerSearchDto,
    val foodAllergies: List<String>?,
    val medicalDietaryRequirements: List<String>?,
    val personalisedDietaryRequirements: List<String>?,
    val cateringInstructions: String?,
    val incentives: PrisonerIncentivesDto?,
    val hasActiveAdjudications: Boolean,
    val activeAdjudications: List<Punishment>?,
  )
}
