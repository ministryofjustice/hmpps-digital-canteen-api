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
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateCartRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaCompleteCartResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentResult
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException

@Component
class MedusaStoreClient(
  @Qualifier("medusaStoreWebClient") private val medusaStoreClient: WebClient,
  private val errorHandler: WebClientErrorHandler,
) {

  companion object {
    val logger: Logger = LoggerFactory.getLogger(MedusaStoreClient::class.java)
  }

  fun createCart(createCartRequest: CreateCartRequest): CartResponse = medusaStoreClient
    .post()
    .uri("/store/pin-phone/carts")
    .bodyValue(createCartRequest)
    .retrieve()
    .bodyToMono(CartResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      val errorResponse = errorHandler.handleError(ex)
      logger.error("Create cart failed: ${ex.responseBodyAsString}")
      UpstreamException(errorResponse.userMessage ?: "Create cart failed")
    }
    .block()!!

  fun addPinPhoneItemsToCart(addItemsRequest: AddItemsRequest, cartId: String): CartResponse = medusaStoreClient
    .post()
    .uri("/store/pin-phone/carts/$cartId/add-items")
    .bodyValue(addItemsRequest)
    .retrieve()
    .bodyToMono(CartResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      val errorResponse = errorHandler.handleError(ex)
      logger.error("Add line item request failed: ${ex.responseBodyAsString}")
      UpstreamException(errorResponse.userMessage ?: "Add line item request failed")
    }
    .block()!!

  //todo Moving towards a single medusaapi openapi spec, this will be updated as part of CRC-557
  fun completeCart(cartId: String, request: PaymentResult): MedusaCompleteCartResponse = medusaStoreClient
    .post()
    .uri("/store/pin-phone/carts/$cartId/complete")
    .bodyValue(mapOf<String, Any>("PaymentResult" to request))
    .retrieve()
    .bodyToMono(MedusaCompleteCartResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      val errorResponse = errorHandler.handleError(ex)
      logger.error("Cart completion failed: ${ex.responseBodyAsString}")
      UpstreamException(errorResponse.userMessage ?: "Cart completion failed")
    }
    .block()!!
}
