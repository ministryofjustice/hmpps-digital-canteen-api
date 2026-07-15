package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldClientRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentResult
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentStatus
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateClientTransactionRequest
import java.util.logging.Logger
import kotlin.String

@Service
class PinPhoneBuyCreditOrchestrationService(
  private val financeService: PrisonFinanceService,
  private val prisonerEnrichmentService: PrisonerEnrichmentService,
  private val medusaStoreClient: MedusaStoreClient
 // private val btApiClient: BtApiClient // Added missing BT API dependency
) {
  private val log = Logger.getLogger(PinPhoneBuyCreditOrchestrationService::class.java.name)

  fun processCheckout(offenderNo: String, amount: Number, cartId: String): String {

      val prisonerInfo = prisonerEnrichmentService.getEnrichedPrisoner(offenderNo).block() ?:
          throw ResponseStatusException(HttpStatus.NOT_FOUND, "Prisoner not found")

      val prisonId = prisonerInfo.prisoner.prisonId
    val transactionResponse = null
    val holdResponse = financeService.addHold(prisonId, offenderNo, AddHoldClientRequest(amount))
      log.info("Hold added for prisoner $offenderNo with hold number ${holdResponse.holdNumber}")
      try {
        //call add credit BT API
        //if success call create transaction
        val transactionResponse = financeService.releaseHoldAndCreateTransaction(prisonId, offenderNo, holdResponse.holdNumber,
          ReleaseHoldCreateClientTransactionRequest(transactionType = "PHONE")
        )
        log.info("Transaction created for prisoner $offenderNo with transaction id ${transactionResponse.id}")
        val request = PaymentResult(
          offender_no = offenderNo,
          status = PaymentStatus.AUTHORIZED,
          transactionBatchNumber = "1234567890",
          transactionReference = "1234567890",
          holdNumber = holdResponse.holdNumber.toString(),
          errorCode = null,
          errorMessage = null
        )
        //call medusa store API complete cart
        val result = medusaStoreClient.completeCart(cartId, request).block()
        log.info("Medusa Store API response: $result")
        return PaymentStatus.AUTHORIZED.toString()
      } catch (e: Exception) {
        //release hold
        financeService.releaseHold(prisonId, offenderNo, holdResponse.holdNumber)

        val request = PaymentResult(
          offender_no = offenderNo,
          status = PaymentStatus.ERROR,
          transactionBatchNumber = null,
          transactionReference = null,
          holdNumber = holdResponse.holdNumber.toString(),
          errorCode = null,
          errorMessage = null
        )
        //call medusa store API complete cart with error
        val result = medusaStoreClient.completeCart(cartId, request).block()
        log.info("Medusa Store API response: $result")
        return PaymentStatus.ERROR.toString()
      }
  }
}

