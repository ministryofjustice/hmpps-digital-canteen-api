package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateTransactionRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateTransactionResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Component
class PrisonFinanceClient(
  @Qualifier("prisonApiWebClient") private val webClient: WebClient,
  private val objectMapper: ObjectMapper,
) {
  val logger: Logger = LoggerFactory.getLogger(PrisonFinanceClient::class.java)

  @Suppress("ktlint:standard:function-expression-body")
  fun addHold(
    prisonId: String,
    offenderNo: String,
    request: AddHoldRequest,
  ): AddHoldResponse {
    return try {
      webClient.post()
        .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", prisonId, offenderNo)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(AddHoldResponse::class.java)
        .block()!!
    } catch (ex: WebClientResponseException) {
      val error = handleError(ex)
      logger.error("AddHold request failed for offenderNo: $offenderNo", error)
      throw UpstreamException(error.userMessage ?: "AddHold request failed")
    }
  }

  @Suppress("ktlint:standard:function-expression-body")
  fun releaseHold(
    prisonId: String,
    offenderNo: String,
    holdNumber: Number,
    request: ReleaseHoldRequest,
  ): ResponseEntity<Void> {
    return try {
      webClient.post()
        .uri(
          "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold/{holdNumber}",
          prisonId,
          offenderNo,
          holdNumber,
        )
        .bodyValue(request)
        .retrieve()
        .toBodilessEntity()
        .block()!!
    } catch (ex: WebClientResponseException) {
      val errorResponse = handleError(ex)
      logger.error("ReleaseHold request failed for offenderNo: $offenderNo", errorResponse)
      throw UpstreamException(errorResponse.userMessage ?: "ReleaseHold request failed")
    }
  }

  @Suppress("ktlint:standard:function-expression-body")
  fun releaseHoleCreateTransaction(
    prisonId: String,
    offenderNo: String,
    holdNumber: Number,
    request: ReleaseHoldCreateTransactionRequest,
  ): ReleaseHoldCreateTransactionResponse {
    return try {
      webClient.post()
        .uri(
          "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
          prisonId,
          offenderNo,
          holdNumber,
        )
        .bodyValue(request)
        .retrieve()
        .bodyToMono(ReleaseHoldCreateTransactionResponse::class.java)
        .block()!!
    } catch (ex: WebClientResponseException) {
      val errorResponse = handleError(ex)
      logger.error("ReleaseHoldCreateTransaction request failed for offenderNo: $offenderNo", errorResponse)
      throw UpstreamException(errorResponse.userMessage ?: "ReleaseHoldCreateTransaction request failed")
    }
  }

  private fun handleError(ex: WebClientResponseException): ErrorResponse = try {
    objectMapper.readValue(ex.responseBodyAsString, ErrorResponse::class.java)
  } catch (parseException: Exception) {
    logger.error("Failed to parse error response body for status: ${ex.statusCode}", parseException)
    ErrorResponse(
      status = ex.statusCode.value(),
      errorCode = "UNKNOWN",
      userMessage = "Unable to parse error response from server.",
      developerMessage = parseException.message ?: "Error parsing response body.",
      moreInfo = "No additional information available.",
    )
  }
}
