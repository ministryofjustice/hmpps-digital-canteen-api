package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.PrisonerIncentivesDto

@Component
class PrisonerIncentivesClient(
  @Qualifier("prisonerIncentivesWebClient") private val prisonerIncentivesWebClient: WebClient,
) {

  fun getPrisoner(prisonerNumber: String): Mono<PrisonerIncentivesDto> = prisonerIncentivesWebClient
    .get()
    .uri("/incentive-reviews/prisoner/{prisonerNumber}", prisonerNumber)
    .retrieve()
    .bodyToMono()
}
