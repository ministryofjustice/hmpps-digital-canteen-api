package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.OpaInput
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.OpaResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.ProductDecision

@Service
class OpaService (private val opaWebClient: WebClient,
                  private val prisonerEnrichmentService: PrisonerEnrichmentService,
                  private val productEnrichmentInfoService: ProductEnrichmentInfoService
) {
  fun evaluateProducts(prisonerNumber: String): ProductDecision {

   /* val objectMapper = jacksonObjectMapper()
      .registerModule(JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)*/

    val prisoner = prisonerEnrichmentService.getEnrichedPrisoner(prisonerNumber)
      .block()!!

    val product = productEnrichmentInfoService.getProductEnrichmentDetails("5400873265578")
      .block()!!

    val input = mapOf("input" to OpaInput(
      prisoner = prisoner,
      product = product)
    )
    //println(objectMapper.writeValueAsString(input))

    val response = opaWebClient.post()
      .uri("/v1/data/ecommerce/visibility/internal/decision")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(input)
      .retrieve()
      .bodyToMono(OpaResponse::class.java)
      .block()

    println("OPA RESPONSE = $response")

    return response?.result?.firstOrNull()
      ?: throw IllegalStateException("OPA returned no decision")
  }
}

