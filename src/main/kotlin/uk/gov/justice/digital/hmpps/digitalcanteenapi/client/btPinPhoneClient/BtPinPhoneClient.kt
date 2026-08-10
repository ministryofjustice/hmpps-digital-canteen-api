package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBuyCreditRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneControlledNumbersRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneControlledNumbersResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtTokenRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtTokenResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Component
class BtPinPhoneClient(
  @Qualifier("btPinPhoneWebClient") private val btPinPhoneWebClient: WebClient,
  @Value("\${bt.client.id}") private val clientId: String,
  @Value("\${bt.client.secret}") private val clientSecret: String,
  private val objectMapper: ObjectMapper,
) {

  companion object {
    private val logger = LoggerFactory.getLogger(BtPinPhoneClient::class.java)
  }

  fun getBtToken(): Mono<BtTokenResponse> = btPinPhoneWebClient
    .post()
    .uri("/auth/token")
    .bodyValue(BtTokenRequest(clientId = clientId, clientSecret = clientSecret))
    .retrieve()
    .bodyToMono<BtTokenResponse>()
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      val error = handleError(ex)
      logger.error("BT auth token request failed: ${error.userMessage}")
      UpstreamException(error.userMessage ?: "Auth token request failed")
    }

  fun getPrisonerBalance(btPinPhoneBalanceRequest: BtPinPhoneBalanceRequest): Mono<BtPinPhoneBalanceResponse> = getBtToken().flatMap { btAuthResponse ->
    btPinPhoneWebClient
      .post()
      .uri("/pcs/Balance")
      .headers { it.setBearerAuth(btAuthResponse.accessToken) }
      .bodyValue(btPinPhoneBalanceRequest)
      .retrieve()
      .bodyToMono<BtPinPhoneBalanceResponse>()
      .onErrorMap(WebClientResponseException::class.java) { ex ->
        val error = handleError(ex)
        logger.error("BT balance request failed for prisoner ${btPinPhoneBalanceRequest.prisonerId}: ${error.userMessage}")
        UpstreamException(error.userMessage ?: "Balance request failed")
      }
  }

  fun getPrisonerContacts(btPinPhoneControlledNumbersRequest: BtPinPhoneControlledNumbersRequest): Mono<BtPinPhoneControlledNumbersResponse> = getBtToken().flatMap { btAuthResponse ->
    btPinPhoneWebClient
      .post()
      .uri("/pcs/ControlledNumbers")
      .headers { it.setBearerAuth(btAuthResponse.accessToken) }
      .bodyValue(btPinPhoneControlledNumbersRequest)
      .retrieve()
      .bodyToMono<BtPinPhoneControlledNumbersResponse>()
      .onErrorResume(WebClientResponseException.BadRequest::class.java) { ex ->
        logger.error("BT response: ${ex.responseBodyAsString}")
        Mono.error(ex)
      }
  }

  @Suppress("UnusedParameter")
  fun addCredit(btPinPhoneBuyCreditRequest: BtPinPhoneBuyCreditRequest): Mono<Void> = Mono.empty()

  private fun handleError(ex: WebClientResponseException): ErrorResponse = try {
    objectMapper.readValue(ex.responseBodyAsString, ErrorResponse::class.java)
  } catch (parseException: JacksonException) {
    logger.error("Failed to parse BT error response for status: ${ex.statusCode}", parseException)
    ErrorResponse(
      status = ex.statusCode.value(),
      errorCode = "UNKNOWN",
      userMessage = "Unable to parse error response from BT (${ex.statusCode}).",
      developerMessage = parseException.message ?: "Error parsing response body.",
      moreInfo = "No additional information available.",
    )
  }
}
