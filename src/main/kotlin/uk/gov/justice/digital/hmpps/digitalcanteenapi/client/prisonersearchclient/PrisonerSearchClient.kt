package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerSearch.generated.Prisoner
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient.Companion.logger
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException

@Component
class PrisonerSearchClient(
  @Qualifier("prisonerSearchWebClient") private val prisonerSearchClient: WebClient,
  private val errorHandler: WebClientErrorHandler,
) {

  fun getPrisoner(prisonerNumber: String): Mono<Prisoner> = prisonerSearchClient.get()
    .uri("/prisoner/{prisonerNumber}", prisonerNumber)
    .retrieve()
    .bodyToMono(Prisoner::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      val errorResponse = errorHandler.handleError(ex)
      logger.error("GET prisoner request failed for prisoner: $prisonerNumber", errorResponse)
      UpstreamException(errorResponse.userMessage ?: "getPrisoner request failed")
    }
}
