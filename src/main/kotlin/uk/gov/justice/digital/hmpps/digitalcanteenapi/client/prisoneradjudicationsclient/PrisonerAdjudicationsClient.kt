package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient.dto.AdjudicationsPunishmentDto

@Component
class PrisonerAdjudicationsClient(
  @Qualifier("prisonerAdjudicationsWebClient") private val prisonerAdjudicationsDto: WebClient,
) {

  @Suppress("MaxLineLength")
  fun getPrisonerAdjudication(offenderBookingId: String): Mono<List<AdjudicationsPunishmentDto>> = prisonerAdjudicationsDto
    .get()
    .uri("/reported-adjudications/punishments/{offenderBookingId}/active", offenderBookingId)
    .retrieve()
    .bodyToMono()
}
