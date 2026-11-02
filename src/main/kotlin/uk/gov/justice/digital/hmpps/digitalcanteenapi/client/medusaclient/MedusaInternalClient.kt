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
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateMedusaUserRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.controller.MedusaController
import kotlin.jvm.java

@Component
class MedusaInternalClient(
  @Qualifier("medusaInternalWebClient") private val medusaInternalClient: WebClient,
  private val errorHandler: WebClientErrorHandler,
) {

  companion object {
    val logger: Logger = LoggerFactory.getLogger(MedusaInternalClient::class.java)
  }

  fun createMedusaUser(createMedusaUserRequest: CreateMedusaUserRequest): ResponseEntity<Map<String, String>> = medusaInternalClient
    .post()
    .uri("/internal/canteen/create-user")
    .bodyValue(createMedusaUserRequest)
    .retrieve()
    .toBodilessEntity()
    .map { ResponseEntity.ok(mapOf("status" to "success")) }
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      val errorResponse = errorHandler.handleError(ex)
      if (ex.statusCode.is5xxServerError) {
        logger.error("Create medusa user failed: ${ex.responseBodyAsString}")
        UpstreamException(errorResponse.userMessage ?: "Medusa service is currently unavailable")
      } else {
        logger.error("Create medusa user failed: ${ex.responseBodyAsString}")
        UpstreamException(errorResponse.userMessage ?: "Create medusa user failed")
      }
    }
    .onErrorMap(WebClientRequestException::class.java) { ex ->
      logger.error("Create medusa user failed due to connection issue", ex)
      UpstreamException("Medusa service is currently unavailable")
    }
    .block()!!

  fun getBasket(getBasketRequest: MedusaController.GetBasketRequest): Any = medusaInternalClient
    .post()
    .uri("/internal/canteen/get-basket")
    .bodyValue(getBasketRequest)
    .retrieve()
    .bodyToMono(Any::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      if (ex.statusCode.is5xxServerError) {
        UpstreamException("Medusa service is currently unavailable")
      } else {
        val errorResponse = errorHandler.handleError(ex)
        logger.error("Create cart failed: ${ex.responseBodyAsString}")
        UpstreamException(errorResponse.userMessage ?: "Create cart failed")
      }
    }
    .onErrorMap(WebClientRequestException::class.java) { ex ->
      logger.error("Create cart failed due to connection issue", ex)
      UpstreamException("Medusa service is currently unavailable")
    }
    .block()!!

  fun addLineItem(addLineItemsRequest: AddLineItemsRequest): AddLineItemsResponse = medusaInternalClient
    .post()
    .uri("/internal/canteen/carts/add-line-item")
    .bodyValue(addLineItemsRequest)
    .retrieve()
    .bodyToMono(AddLineItemsResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      if (ex.statusCode.is5xxServerError) {
        UpstreamException("Medusa service is currently unavailable")
      } else {
        val errorResponse = errorHandler.handleError(ex)
        logger.error("Add line items failed: ${ex.responseBodyAsString}")
        UpstreamException(errorResponse.userMessage ?: "Add line items failed")
      }
    }
    .onErrorMap(WebClientRequestException::class.java) { ex ->
      logger.error("Add line items failed due to connection issue", ex)
      UpstreamException("Medusa service is currently unavailable")
    }
    .block()!!

  fun removeLineItem(removeLineItemsRequest: RemoveLineItemsRequest): RemoveLineItemsResponse = medusaInternalClient
    .post()
    .uri("/internal/canteen/carts/remove-line-item")
    .bodyValue(removeLineItemsRequest)
    .retrieve()
    .bodyToMono(RemoveLineItemsResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      if (ex.statusCode.is5xxServerError) {
        UpstreamException("Medusa service is currently unavailable")
      } else {
        val errorResponse = errorHandler.handleError(ex)
        logger.error("Add line items failed: ${ex.responseBodyAsString}")
        UpstreamException(errorResponse.userMessage ?: "Add line items failed")
      }
    }
    .onErrorMap(WebClientRequestException::class.java) { ex ->
      logger.error("Add line items failed due to connection issue", ex)
      UpstreamException("Medusa service is currently unavailable")
    }
    .block()!!

//  fun createCart(createCartRequest: CreateCartRequest): CartResponse = medusaInternalClient
//    .post()
//    .uri("/internal/pin-phone/carts")
//    .bodyValue(createCartRequest)
//    .retrieve()
//    .bodyToMono(CartResponse::class.java)
//    .onErrorMap(WebClientResponseException::class.java) { ex ->
//      if (ex.statusCode.is5xxServerError) {
//        UpstreamException("Medusa service is currently unavailable")
//      } else {
//        val errorResponse = errorHandler.handleError(ex)
//        logger.error("Create cart failed: ${ex.responseBodyAsString}")
//        UpstreamException(errorResponse.userMessage ?: "Create cart failed")
//      }
//    }
//    .onErrorMap(WebClientRequestException::class.java) { ex ->
//      logger.error("Create cart failed due to connection issue", ex)
//      UpstreamException("Medusa service is currently unavailable")
//    }
//    .block()!!
//
//  fun addPinPhoneItemsToCart(addItemsRequest: AddItemsRequest, cartId: String): CartResponse = medusaInternalClient
//    .post()
//    .uri("/internal/pin-phone/carts/$cartId/add-items")
//    .bodyValue(addItemsRequest)
//    .retrieve()
//    .bodyToMono(CartResponse::class.java)
//    .onErrorMap(WebClientResponseException::class.java) { ex ->
//      if (ex.statusCode.is5xxServerError) {
//        UpstreamException("Medusa service is currently unavailable")
//      } else {
//        val errorResponse = errorHandler.handleError(ex)
//        logger.error("Add line item request failed: ${ex.responseBodyAsString}")
//        UpstreamException(errorResponse.userMessage ?: "Add line item request failed")
//      }
//    }
//    .onErrorMap(WebClientRequestException::class.java) { ex ->
//      logger.error("Add line item failed due to connection issue", ex)
//      UpstreamException("Medusa service is currently unavailable")
//    }
//    .block()!!
//
//  fun completeCart(cartId: String, paymentRequest: PaymentRequest): CompleteCartResponse = medusaInternalClient
//    .post()
//    .uri("/internal/pin-phone/carts/$cartId/complete")
//    .bodyValue(mapOf("PaymentRequest" to paymentRequest))
//    .retrieve()
//    .bodyToMono(CompleteCartResponse::class.java)
//    .onErrorMap(WebClientResponseException::class.java) { ex ->
//      if (ex.statusCode.is5xxServerError) {
//        UpstreamException("Medusa service is currently unavailable")
//      } else {
//        val errorResponse = errorHandler.handleError(ex)
//        logger.error("Cart completion failed: ${ex.responseBodyAsString}")
//        UpstreamException(errorResponse.userMessage ?: "Cart completion failed")
//      }
//    }
//    .onErrorMap(WebClientRequestException::class.java) { ex ->
//      logger.error("Cart completion failed due to connection issue", ex)
//      UpstreamException("Medusa service is currently unavailable")
//    }
//    .block()!!
}
