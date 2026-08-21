package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CartResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaCompleteCartResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaCreateCartRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaCreateCartResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentResult
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.controller.AddItemsToCartRequest

@Component
class MedusaStoreClient(
  @Qualifier("medusaStoreWebClient") private val medusaStoreClient: WebClient,
  private val errorHandler: WebClientErrorHandler,
) {

  companion object {
    val logger: Logger = LoggerFactory.getLogger(MedusaStoreClient::class.java)
  }

  fun createCart(cartRequest: MedusaCreateCartRequest): MedusaCreateCartResponse = medusaStoreClient
    .post()
    .uri("/store/pin-phone/carts")
    .bodyValue(cartRequest)
    .retrieve()
    .bodyToMono(MedusaCreateCartResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      println(ex)
      val errorResponse = errorHandler.handleError(ex)
      logger.error("Create cart failed: ${ex.responseBodyAsString}")
      UpstreamException(errorResponse.userMessage ?: "Create cart failed")
    }
    .block()!!

  fun addPinPhoneItemsToCart(addItemsToCartRequest: AddItemsToCartRequest): CartResponse = medusaStoreClient
    .post()
    .uri("/store/pin-phone/carts/${addItemsToCartRequest.cartId}/add-items")
    .bodyValue(AddItemsRequest(amount = addItemsToCartRequest.amount))
    .retrieve()
    .bodyToMono(CartResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      println(ex)
      val errorResponse = errorHandler.handleError(ex)
      logger.error("Add line item request failed: ${ex.responseBodyAsString}")
      UpstreamException(errorResponse.userMessage ?: "Add line item request failed")
    }
    .block()!!

  fun completeCart(cartId: String, request: PaymentResult): MedusaCompleteCartResponse = medusaStoreClient
    .post()
    .uri("/store/pin-phone/carts/$cartId/complete")
    .bodyValue(mapOf<String, Any>("PaymentResult" to request))
    .retrieve()
    .bodyToMono(MedusaCompleteCartResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      println(ex)
      val errorResponse = errorHandler.handleError(ex)
      logger.error("Cart completion failed: ${ex.responseBodyAsString}")
      UpstreamException(errorResponse.userMessage ?: "Cart completion failed")
    }
    .block()!!
}
