package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.opa

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient.Companion.logger
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.logicengine.request.OpaRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.logicengine.response.OpaResponse

@Component
class OpaClient(
  @Qualifier("opaWebClient") private val opaClient: WebClient,
  private val errorHandler: WebClientErrorHandler,
) {

  fun evaluate(request: OpaRequest) = opaClient.post()
    .uri("/v1/data/app/main/response")
    .bodyValue(request)
    .retrieve()
    .bodyToMono(OpaResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      val errorResponse = errorHandler.handleError(ex)
      logger.error("Rule evaluation failed: ${ex.responseBodyAsString}")
      UpstreamException(errorResponse.userMessage ?: "Rule evaluation failed")
    }
    .block()!!
}
