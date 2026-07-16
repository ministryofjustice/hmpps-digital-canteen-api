package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldClientRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentResult
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentStatus
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateClientTransactionRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import kotlin.String

@Service
class PinPhoneBuyCreditOrchestrationService(
  private val financeService: PrisonFinanceService,
  private val prisonerEnrichmentService: PrisonerEnrichmentService,
  private val medusaStoreClient: MedusaStoreClient,
) {
  private val log: Logger = LoggerFactory.getLogger("PinPhoneBuyCreditOrchestrationService")

  fun processCheckout(offenderNo: String, amount: Number, cartId: String): String {
    val prisonerInfo = prisonerEnrichmentService.getEnrichedPrisoner(offenderNo).block()
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Prisoner not found")

    val prisonId = prisonerInfo.prisoner.prisonId
    val holdResponse = financeService.addHold(prisonId, offenderNo, AddHoldClientRequest(amount))
    log.info("Hold added for prisoner {} with hold number {}", offenderNo, holdResponse.holdNumber)

    return try {
      // TODO: call add credit BT API
      // if success call create transaction
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
        transactionReference = "1234567890", // TODO: Replace with actual reference
        holdNumber = holdResponse.holdNumber.toString(),
        errorCode = null,
        errorMessage = null,
      )
      log.info("Completing cart {} for prisoner {}",request, offenderNo)
      medusaStoreClient.completeCart(cartId, request)
      log.info("Successfully completed cart {} for prisoner {}", cartId, offenderNo)
      PaymentStatus.AUTHORIZED.toString()
    } catch (e: UpstreamException) {
      log.error("Upstream error processing checkout for prisoner {}: {}", offenderNo, e.message)
      handleCheckoutError(prisonId, offenderNo, holdResponse.holdNumber, cartId, e.message)
    } catch (e: Exception) {
      log.error("Failed to process checkout for prisoner {}: {}", offenderNo, e.message, e)
      handleCheckoutError(prisonId, offenderNo, holdResponse.holdNumber, cartId, e.message)
    }
  }

  private fun handleCheckoutError(
    prisonId: String?,
    offenderNo: String,
    holdNumber: Number,
    cartId: String,
    errorMessage: String?,
  ): String {
    try {
      financeService.releaseHold(prisonId, offenderNo, holdNumber)
    } catch (e: UpstreamException) {

      val request = PaymentResult(
        offender_no = offenderNo,
        status = PaymentStatus.ERROR,
        transactionReference = null,
        holdNumber = holdNumber.toString(),
        errorCode = null,
        errorMessage = errorMessage,
      )

      medusaStoreClient.completeCart(cartId, request)
    }
    return PaymentStatus.ERROR.toString()
  }
}

