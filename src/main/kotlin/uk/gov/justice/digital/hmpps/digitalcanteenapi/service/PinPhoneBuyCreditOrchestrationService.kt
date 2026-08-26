package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBuyCreditRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CartResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CompleteCartOrderResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateCartRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.PaymentRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.ReleaseHoldAndCreateTransaction
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

  fun processCheckout(paymentRequest: PaymentRequest, cartId: String): CompleteCartOrderResponse {
    println(paymentRequest)
    requireNotNull(paymentRequest.offenderNo) { "amountPence must not be null" }

    val prisonId = pinPhonePrisonerEnrichmentService.getEnrichedPrisoner(paymentRequest.offenderNo).block()
      ?. also { log.info("Processing checkout for prisoner") }
      ?. prisoner
      ?. prisonId
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Prisoner not found")

    requireNotNull(paymentRequest.amountPence) { "amountPence must not be null" }

    val holdResponse = financeService.addHold(prisonId, paymentRequest.offenderNo, paymentRequest.amountPence)
    log.info("Hold added for prisoner {} with hold number {}", paymentRequest.offenderNo, holdResponse.holdNumber)

    return try {
      callBtApiWithRetry(paymentRequest.offenderNo, paymentRequest.amountPence.toInt())

      val transactionResponse = financeService.releaseHoldAndCreateTransaction(
        prisonId,
        paymentRequest.offenderNo,
        holdResponse.holdNumber,
        ReleaseHoldAndCreateTransaction.Type.PHONE,
      )
      log.info("Transaction created for prisoner {} with transaction id {}", paymentRequest.offenderNo, transactionResponse.id)

      val request = PaymentRequest(
        offenderNo = paymentRequest.offenderNo,
        status = PaymentRequest.Status.AUTHORIZED,
        transactionReference = "1234567890", // later Replace it with actual reference
        holdNumber = holdResponse.holdNumber,
        amountPence = paymentRequest.amountPence,
        errorCode = null,
        errorMessage = null,
      )
      var response =  medusaStoreClient.completeCart(cartId, request)
      log.info(response.toString())
      return response

    } catch (e: WebClientResponseException) { // added to handled the medusa store error
      val jsonString = e.responseBodyAsString
      val mapper = jacksonObjectMapper()
      val jsonMap = mapper.readValue<Map<String, Any>>(jsonString)
      val message = jsonMap["message"] as? String

      CompleteCartOrderResponse(
        successful = false,
        cartId = cartId,
      )
    } catch (e: UpstreamException) {
      log.error("Upstream error processing checkout for prisoner {}: {}", paymentRequest.offenderNo, e.message)
      handleCheckoutError(prisonId, paymentRequest.offenderNo, holdResponse.holdNumber, cartId, e.message)
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
    holdNumber: Long,
    cartId: String,
    errorMessage: String?,
  ): CompleteCartOrderResponse {
    try {
      log.info("Releasing hold for prisoner {} with hold number {}", offenderNo, holdNumber)
      financeService.releaseHold(prisonId, offenderNo, holdNumber)
    } catch (e: UpstreamException) {
      log.error("Failed to release hold for prisoner {} with hold number {}: {}", offenderNo, holdNumber, e.message)
    }

    val request = PaymentRequest(
      offenderNo = offenderNo,
      status = PaymentRequest.Status.ERROR,
      transactionReference = null,
      holdNumber = holdNumber,
      errorCode = null,
      errorMessage = errorMessage,
    )
    return medusaStoreClient.completeCart(cartId, request)
  }

  fun createCart(request: CreateCartRequest): ResponseEntity<CartResponse> {
    log.info("Creating cart for offender {}", request.metadata.offenderNo)
    val response = medusaStoreClient.createCart(request)
    log.info("Successfully created cart {}", response.cart?.id)
    return ResponseEntity.ok(response)
  }
}
