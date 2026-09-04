package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.opa

import org.springframework.beans.factory.annotation.Qualifier
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.logicengine.request.OpaRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.logicengine.response.OpaResponse

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class OpaClient(
  @Qualifier("opaWebClient") private val opaClient: WebClient,
) {

  fun evaluate(request: OpaRequest): OpaResponse? {
    return opaClient.post()
      .uri("/v1/data/app/main/response")
      .bodyValue(request)
      .retrieve()
      .bodyToMono(OpaResponse::class.java
      )
  }
}