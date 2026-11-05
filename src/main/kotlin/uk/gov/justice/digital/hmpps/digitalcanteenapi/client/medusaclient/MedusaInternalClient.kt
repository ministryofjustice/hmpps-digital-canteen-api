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
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetBasketRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetBasketResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveBasketRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
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

  fun getBasket(getBasketRequest: GetBasketRequest): GetBasketResponse = medusaInternalClient
    .post()
    .uri("/internal/canteen/carts/get-basket")
    .bodyValue(getBasketRequest)
    .retrieve()
    .bodyToMono(GetBasketResponse::class.java)
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
        logger.error("Remove line items failed: ${ex.responseBodyAsString}")
        UpstreamException(errorResponse.userMessage ?: "Add line items failed")
      }
    }
    .onErrorMap(WebClientRequestException::class.java) { ex ->
      logger.error("Remove line items failed due to connection issue", ex)
      UpstreamException("Medusa service is currently unavailable")
    }
    .block()!!

  fun removeBasket(removeBasketRequest: RemoveBasketRequest): ResponseEntity<Map<String, String>> = medusaInternalClient
    .post()
    .uri("/internal/canteen/carts/remove-basket")
    .bodyValue(removeBasketRequest)
    .retrieve()
    .toBodilessEntity()
    .map { ResponseEntity.ok(mapOf("status" to "success")) }
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      val errorResponse = errorHandler.handleError(ex)
      if (ex.statusCode.is5xxServerError) {
        logger.error("Medusa service is currently unavailable")
        UpstreamException(errorResponse.userMessage ?: "Medusa service is currently unavailable")
      } else {
        logger.error("Medusa basket removal failed: ${ex.responseBodyAsString}")
        UpstreamException(errorResponse.userMessage ?: "Create medusa user failed")
      }
    }
    .onErrorMap(WebClientRequestException::class.java) { ex ->
      logger.error("Medusa basket removal failed due to connection issue", ex)
      UpstreamException("Medusa service is currently unavailable")
    }
    .block()!!
}
