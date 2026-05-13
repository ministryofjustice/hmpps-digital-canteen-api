package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.HealthAndMedicationClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerAdjudicationsClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerIncentivesClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.*
import java.time.LocalDate
import java.util.Optional

@Component
class PrisonerEnrichmentService(
  private val healthAndMedicationClient: HealthAndMedicationClient,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonerAdjudicationsClient: PrisonerAdjudicationsClient,
  private val prisonerIncentivesClient: PrisonerIncentivesClient,
) {

  fun getEnrichedPrisoner(prisonerNumber: String): Mono<EnrichedPrisonerDto> {

    val prisonerMono =
      prisonerSearchClient.getPrisoner(prisonerNumber)

    val healthMono =
      healthAndMedicationClient.getPrisoner(prisonerNumber)
        .onErrorResume { Mono.empty() }

    val incentivesMono =
      prisonerIncentivesClient.getPrisoner(prisonerNumber)
        .onErrorResume { Mono.empty() }

    val adjudicationsMono =
      prisonerMono
        .flatMap { prisoner -> prisoner.bookingId?.let { bookingId -> prisonerAdjudicationsClient
          .getPrisonerHasAdjudication(bookingId) } ?: Mono.empty() }
        .onErrorResume { Mono.empty() }

    return Mono.zip(
      prisonerMono,
      healthMono
        .map { Optional.of(it) }
        .defaultIfEmpty(Optional.empty()),
      adjudicationsMono
        .map { Optional.of(it) }
        .defaultIfEmpty(Optional.empty()),
      incentivesMono
        .map { Optional.of(it) }
        .defaultIfEmpty(Optional.empty()),
    )
      .map { tuple ->
        EnrichedPrisonerDto(
          prisoner = tuple.t1,
          healthAndMedication = tuple.t2.orElse(null),
          adjudications = tuple.t3.orElse(null),
          incentives = tuple.t4.orElse(null),
        )
      }
  }
}

  data class EnrichedPrisonerDto(
    val prisoner: PrisonerSearchDto,
    val healthAndMedication: HealthAndMedicationDto?,
    val adjudications: PrisonerHasAdjudicationsDto?,
    val incentives: PrisonerIncentivesDto?
  )

