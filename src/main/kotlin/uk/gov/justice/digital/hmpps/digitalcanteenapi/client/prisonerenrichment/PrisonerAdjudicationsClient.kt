package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.Punishment

@Component
class PrisonerAdjudicationsClient(
  @Qualifier("prisonerAdjudicationsWebClient") private val prisonerAdjudicationsDto: WebClient,
) {

  fun getPrisonerAdjudication(offenderBookingId: String): Mono<List<Punishment>> = prisonerAdjudicationsDto
    .get()
    .uri("/reported-adjudications/punishments/{offenderBookingId}/active", offenderBookingId)
    .retrieve()
    .bodyToMono()
}
