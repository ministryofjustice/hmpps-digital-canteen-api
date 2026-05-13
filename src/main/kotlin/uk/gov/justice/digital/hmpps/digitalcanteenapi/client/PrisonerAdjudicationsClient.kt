package uk.gov.justice.digital.hmpps.digitalcanteenapi.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.PrisonerAdjudicationsDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.PrisonerHasAdjudicationsDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.PrisonerSearchDto

@Component
class PrisonerAdjudicationsClient(
  @Qualifier("prisonerAdjudicationsWebClient") private val prisonerAdjudicationsDto: WebClient,
  @Qualifier("prisonerAdjudicationsWebClient") private val prisonerHasAdjudicationsDto: WebClient,
) {

  fun getPrisonerHasAdjudication(bookingNumber: String): Mono<PrisonerHasAdjudicationsDto> =
    prisonerHasAdjudicationsDto
      .get()
      .uri("/adjudications/booking/{bookingId}/exists", bookingNumber)
      .retrieve()
      .bodyToMono()

  fun getPrisonerAdjudication(prisonerNumber: String): Mono<List<PrisonerAdjudicationsDto>> =
    prisonerAdjudicationsDto
      .get()
      .uri("/reported-adjudications/prisoner/{prisonerNumber}", prisonerNumber)
      .retrieve()
      .bodyToMono()
}