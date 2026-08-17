package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.Account
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.AddHoldTransaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.HoldDetails
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.ReleaseHoldAndCreateTransaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.ReleaseHoldTransaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.Transaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Component
class PrisonFinanceClient(
  @Qualifier("prisonApiWebClient") private val prisonerApiClient: WebClient,
  private val objectMapper: ObjectMapper,
) {
  companion object {
    val logger: Logger = LoggerFactory.getLogger(PrisonFinanceClient::class.java)
  }

  @Suppress("ktlint:standard:function-expression-body")
  fun addHold(
    prisonId: String,
    offenderNo: String,
    request: AddHoldTransaction,
  ): HoldDetails {
    return try {
      prisonerApiClient.post()
        .uri("/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/add-hold", prisonId, offenderNo)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(HoldDetails::class.java)
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
    request: ReleaseHoldTransaction,
  ): ResponseEntity<Void> {
    return try {
      prisonerApiClient.post()
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
  fun releaseHoldCreateTransaction(
    prisonId: String,
    offenderNo: String,
    holdNumber: Number,
    request: ReleaseHoldAndCreateTransaction,
  ): Transaction {
    return try {
      prisonerApiClient.post()
        .uri(
          "/api/finance-holds/prison/{prisonId}/offenders/{offenderNo}/release-hold-transaction/{holdNumber}",
          prisonId,
          offenderNo,
          holdNumber,
        )
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Transaction::class.java)
        .block()!!
    } catch (ex: WebClientResponseException) {
      val errorResponse = handleError(ex)
      logger.error("ReleaseHoldCreateTransaction request failed for offenderNo: $offenderNo", errorResponse)
      throw UpstreamException(errorResponse.userMessage ?: "ReleaseHoldCreateTransaction request failed")
    }
  }

  @Suppress("ktlint:standard:function-expression-body")
  fun getPrisonerBalance(bookingId: String): Mono<Account> {
    return prisonerApiClient.get()
      .uri("/api/bookings/{bookingId}/balances", bookingId)
      .retrieve()
      .bodyToMono(Account::class.java)
      .onErrorMap(WebClientResponseException::class.java) { ex ->
        val errorResponse = handleError(ex)
        logger.error("GET Balance request failed for bookingId: $bookingId", errorResponse)
        UpstreamException(errorResponse.userMessage ?: "getPrisonerBalance request failed")
      }
  }

  private fun handleError(ex: WebClientResponseException): ErrorResponse = try {
    objectMapper.readValue(ex.responseBodyAsString, ErrorResponse::class.java)
  } catch (parseException: JacksonException) {
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
