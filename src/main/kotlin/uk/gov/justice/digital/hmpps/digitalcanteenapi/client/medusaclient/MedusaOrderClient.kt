package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CancelDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetDraftOrderResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException

@Component
class MedusaOrderClient(
  @Qualifier("medusaInternalWebClient") private val medusaInternalClient: WebClient,
  private val errorHandler: WebClientErrorHandler,
) {

  companion object {
    val logger: Logger = LoggerFactory.getLogger(MedusaOrderClient::class.java)
  }

  fun createDraftOrder(createDraftOrderRequest: CreateDraftOrderRequest, cartId: String): CreateDraftOrderResponse = medusaInternalClient
    .post()
    .uri("/internal/canteen/orders/{cartId}/create-draft-order", cartId)
    .bodyValue(createDraftOrderRequest)
    .retrieve()
    .bodyToMono(CreateDraftOrderResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      val errorResponse = errorHandler.handleError(ex)
      if (ex.statusCode.is5xxServerError) {
        logger.error("Medusa service is currently unavailable")
        UpstreamException(errorResponse.userMessage ?: "Medusa service is currently unavailable")
      } else {
        logger.error("Medusa failed to create draft order: ${ex.responseBodyAsString}")
        UpstreamException(errorResponse.userMessage ?: "Medusa failed to create draft order")
      }
    }
    .onErrorMap(WebClientRequestException::class.java) { ex ->
      logger.error("Medusa draft order creation failed due to connection issue", ex)
      UpstreamException("Medusa service is currently unavailable")
    }
    .block()!!

  fun getDraftOrder(orderId: String): GetDraftOrderResponse = medusaInternalClient
    .get()
    .uri("/internal/canteen/orders/{orderId}/get-draft-order", orderId)
    .retrieve()
    .bodyToMono(GetDraftOrderResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      val errorResponse = errorHandler.handleError(ex)
      if (ex.statusCode.is5xxServerError) {
        logger.error("Medusa service is currently unavailable")
        UpstreamException(errorResponse.userMessage ?: "Medusa service is currently unavailable")
      } else {
        logger.error("Medusa failed to get order: ${ex.responseBodyAsString}")
        UpstreamException(errorResponse.userMessage ?: "Medusa failed to get order")
      }
    }
    .onErrorMap(WebClientRequestException::class.java) { ex ->
      logger.error("Medusa order retrieval failed due to connection issue", ex)
      UpstreamException("Medusa service is currently unavailable")
    }
    .block()!!

  fun cancelOrder(cancelDraftOrderRequest: CancelDraftOrderRequest): ResponseEntity<Map<String, String>> = medusaInternalClient
    .post()
    .uri("/internal/canteen/orders/cancel-order")
    .bodyValue(cancelDraftOrderRequest)
    .retrieve()
    .toBodilessEntity()
    .map { ResponseEntity.ok(mapOf("status" to "success")) }
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      val errorResponse = errorHandler.handleError(ex)
      if (ex.statusCode.is5xxServerError) {
        logger.error("Medusa service is currently unavailable")
        UpstreamException(errorResponse.userMessage ?: "Medusa service is currently unavailable")
      } else {
        logger.error("Medusa failed to cancel order: ${ex.responseBodyAsString}")
        UpstreamException(errorResponse.userMessage ?: "Medusa failed to cancel order")
      }
    }
    .onErrorMap(WebClientRequestException::class.java) { ex ->
      logger.error("Medusa order cancellation failed due to connection issue", ex)
      UpstreamException("Medusa service is currently unavailable")
    }
    .block()!!
}
