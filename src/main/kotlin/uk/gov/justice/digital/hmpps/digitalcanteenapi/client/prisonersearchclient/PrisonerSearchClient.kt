package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerSearch.generated.Prisoner

@Component
class PrisonerSearchClient(
  @Qualifier("prisonerSearchWebClient") private val prisonerSearchClient: WebClient,
) {

  fun getPrisoner(prisonerNumber: String): Mono<Prisoner> = prisonerSearchClient
    .get()
    .uri("/prisoner/{prisonerNumber}", prisonerNumber)
    .retrieve()
    .bodyToMono()
}
