package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.dto.Decision
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.dto.OpaInput
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.dto.OpaResponse

@Service
class OpaService(
  @Qualifier("opaWebClient") private val opaWebClient: WebClient,
  private val pinPhonePrisonerEnrichmentService: PinPhonePrisonerEnrichmentService,
) {
  fun evaluatePinPhone(prisonerNumber: String, creditRequested: Int): Mono<Decision> =
    pinPhonePrisonerEnrichmentService.getEnrichedPrisoner(prisonerNumber)
      .flatMap { prisoner ->
        println("test 1" + prisoner.prisonerBalance?.spendsPence)
        println("test 2" + prisoner.prisonerBtBalance?.creditLimitPounds)
        val input = mapOf(

          "input" to OpaInput(
            prisoner = prisoner,
            creditRequested = creditRequested,
          ),
        )
        opaWebClient.post()
          .uri("/v1/data/ecommerce/visibility/internal/decision")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(input)
          .retrieve()
          .bodyToMono(OpaResponse::class.java)
      }
      .flatMap { response ->
        response.result.firstOrNull()
          ?.let { Mono.just(it) }
          ?: Mono.error(IllegalStateException("OPA returned no decision"))
      }
}
