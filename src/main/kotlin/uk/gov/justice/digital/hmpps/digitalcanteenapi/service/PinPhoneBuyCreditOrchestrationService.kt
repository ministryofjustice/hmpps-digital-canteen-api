package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBuyCreditRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldClientRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.CompleteCartResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentResult
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentStatus
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateClientTransactionRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment.PinPhonePrisonerEnrichmentService

@Service
class PinPhoneBuyCreditOrchestrationService(
  private val financeService: PrisonFinanceService,
  private val pinPhonePrisonerEnrichmentService: PinPhonePrisonerEnrichmentService,
  private val medusaStoreClient: MedusaStoreClient,
  private val btPinPhoneClient: BtPinPhoneClient,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger("PinPhoneBuyCreditOrchestrationService")
  }

  fun processCheckout(offenderNo: String, amount: Number, cartId: String): CompleteCartResponse {
    val prisonId = pinPhonePrisonerEnrichmentService.getEnrichedPrisoner(offenderNo).block()
      ?. also { log.info("Processing checkout for prisoner") }
      ?. prisoner
      ?. prisonId
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Prisoner not found")

    val holdResponse = financeService.addHold(prisonId, offenderNo, AddHoldClientRequest(amount))
    log.info("Hold added for prisoner {} with hold number {}", offenderNo, holdResponse.holdNumber)

    return try {
      callBtApiWithRetry(offenderNo, amount.toInt())

      val transactionResponse = financeService.releaseHoldAndCreateTransaction(
        prisonId,
        offenderNo,
        holdResponse.holdNumber,
        ReleaseHoldCreateClientTransactionRequest(transactionType = "PHONE"),
      )
      log.info("Transaction created for prisoner {} with transaction id {}", offenderNo, transactionResponse.id)

      val request = PaymentResult(
        offender_no = offenderNo,
        status = PaymentStatus.AUTHORIZED,
        transactionReference = "1234567890", // later Replace it with actual reference
        holdNumber = holdResponse.holdNumber,
        errorCode = null,
        errorMessage = null,
      )

      val cartResponse = medusaStoreClient.completeCart(cartId, request)
      log.info("Successfully completed cart {} for prisoner {}", cartId, offenderNo)

      // return the following response to the client
      CompleteCartResponse(
        status = "SUCCESS",
        orderId = cartResponse.order?.id,
        message = "Purchase completed successfully.",
      )
    } catch (e: WebClientResponseException) { // added to handled the medusa store error
      val jsonString = e.responseBodyAsString
      val mapper = jacksonObjectMapper()
      val jsonMap = mapper.readValue<Map<String, Any>>(jsonString)
      val message = jsonMap["message"] as? String

      CompleteCartResponse(
        status = PaymentStatus.ERROR.toString(),
        message = message ?: "Medusa store error",
      )
    } catch (e: UpstreamException) {
      log.error("Upstream error processing checkout for prisoner {}: {}", offenderNo, e.message)
      handleCheckoutError(prisonId, offenderNo, holdResponse.holdNumber, cartId, e.message)
    }
  }

  /* TO DO: This needs to refactor based upon the final design decision*/
  private fun callBtApiWithRetry(offenderNo: String, amountPence: Int) {
    var lastException: Exception? = null
    val initialAttempts = 1
    val maxAttempts = 3
    for (i in initialAttempts..maxAttempts) { // Initial call + 2 retries
      try {
        val btPinPhoneBuyCreditRequest = BtPinPhoneBuyCreditRequest(
          reference = "reference_FN",
          prisonerId = offenderNo,
          amountPence = amountPence,
          type = 50,
        )
        btPinPhoneClient.addCredit(btPinPhoneBuyCreditRequest).block()
        log.info("Successfully added credit to BT for prisoner {} on attempt {}", offenderNo, i)
        return
      } catch (e: UpstreamException) {
        log.error("Failed to add credit to BT for prisoner {} on attempt {}: {}", offenderNo, i, e.message)
        lastException = e
      }
    }
    throw lastException ?: RuntimeException("Failed to call BT API")
  }

  private fun handleCheckoutError(
    prisonId: String,
    offenderNo: String,
    holdNumber: Number,
    cartId: String,
    errorMessage: String?,
  ): CompleteCartResponse {
    try {
      log.info("Releasing hold for prisoner {} with hold number {}", offenderNo, holdNumber)
      financeService.releaseHold(prisonId, offenderNo, holdNumber)
    } catch (e: UpstreamException) {
      log.error("Failed to release hold for prisoner {} with hold number {}: {}", offenderNo, holdNumber, e.message)
    }

    val request = PaymentResult(
      offender_no = offenderNo,
      status = PaymentStatus.ERROR,
      transactionReference = null,
      holdNumber = holdNumber,
      errorCode = null,
      errorMessage = errorMessage,
    )
    medusaStoreClient.completeCart(cartId, request)
    return CompleteCartResponse(
      status = PaymentStatus.ERROR.toString(),
      message = errorMessage ?: "Checkout failed",
    )
  }
}
