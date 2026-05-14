package uk.gov.justice.digital.hmpps.digitalcanteenapi.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.HealthAndMedicationDto

@Component
class PrisonerHealthAndMedicationClient(
  @Qualifier("prisonerHealthAndMedicationWebClient") private val prisonerHealthAndMedicationWebClient: WebClient,
) {

  fun getPrisoner(prisonerNumber: String): Mono<HealthAndMedicationDto> = prisonerHealthAndMedicationWebClient
    .get()
    .uri("/prisoners/{prisonerNumber}", prisonerNumber)
    .retrieve()
    .bodyToMono()
}