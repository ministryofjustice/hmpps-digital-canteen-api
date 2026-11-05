package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaInternalClient.Companion.logger
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importnpl.ProductListResponse

@Component
class MedusaProductClient(
  @Qualifier("medusaAdminWebClient")
  private val webClient: WebClient,
  private val errorHandler: WebClientErrorHandler,
  private val objectMapper: ObjectMapper,
) {
  companion object {
    private const val PRODUCTS_URI = "/admin/products"
    private const val BATCH_PRODUCTS_URI = "/admin/products/batch"
    val logger: Logger = LoggerFactory.getLogger(MedusaProductClient::class.java)
  }

  fun getProducts(): ProductListResponse = webClient.get()
    .uri(PRODUCTS_URI)
    .retrieve()
    .onStatus(HttpStatusCode::isError) { response ->
      response.bodyToMono(String::class.java)
        .map { UpstreamException("Failed to fetch products: $it") }
    }
    .bodyToMono(ProductListResponse::class.java)
    .block()!!

  fun batchProducts(request: ProductBatchRequest): ProductBatchResponse = webClient.post()
    .uri(BATCH_PRODUCTS_URI)
    .bodyValue(request)
    .retrieve()
    .bodyToMono(ProductBatchResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      if (ex.statusCode.is5xxServerError) {
        UpstreamException("Medusa service is currently unavailable")
      } else {
        logger.error("Batch update request failed: ${ex.responseBodyAsString}")
        val message = objectMapper.readTree(ex.responseBodyAsString)
          .path("message")
          .asText(ex.message)
        UpstreamException(message ?: "National Product List failed")
      }
    }
    .onErrorMap(WebClientRequestException::class.java) { ex ->
      logger.error("Upload NPL failed due to connection issue", ex)
      UpstreamException("Medusa service is currently unavailable")
    }
    .block()!!
}
