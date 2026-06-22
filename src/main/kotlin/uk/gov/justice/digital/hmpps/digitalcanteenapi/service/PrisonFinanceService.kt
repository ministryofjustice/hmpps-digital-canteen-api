package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldClientRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateClientTransactionRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateTransactionRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateTransactionResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldRequest
import java.util.*
import kotlin.String
/**
 * Service responsible for managing prison finance operations.
 */
@Service
class PrisonFinanceService(
    private val prisonFinanceClient: PrisonFinanceClient,
) {
    private val HOLD_DESCRIPTION = "HOLD"
    private val REMOVE_HOLD_DESCRIPTION = "Remove HOLD"
    private val CLIENT_REFERENCE_PREFIX = "CLIENT-"
    private val CLIENT_TRANSACTION_ID_LENGTH = 12
  /**
   * Adds a hold to the prisoner.
   */
  fun addHold(prisonId: String, offenderNo: String, clientRequest: AddHoldClientRequest): AddHoldResponse {
    val clientReference = generateClientReference()
    val request = AddHoldRequest(
      description = HOLD_DESCRIPTION,
      amount = clientRequest.amount,
      clientTransactionId = clientReference.toClientTransactionId(),
      clientName = offenderNo,
      clientUniqueReference = clientReference.toClientUniqueReference(),
    )

    System.out.println("Request: $request")
    return prisonFinanceClient.addHold(prisonId, offenderNo, request)
  }

  /**
   * Releases a hold.
   */
  fun releaseHold(prisonId: String, offenderNo: String, holdNumber: Number): ResponseEntity<Void> {
    val clientReference = generateClientReference()
    val request = ReleaseHoldRequest(
      description = REMOVE_HOLD_DESCRIPTION,
      clientTransactionId = clientReference.toClientTransactionId(),
      clientName = offenderNo,
      clientUniqueReference = clientReference.toClientUniqueReference(),
    )

    System.out.println("Request: $request")

    return prisonFinanceClient.releaseHold(prisonId, offenderNo, holdNumber, request)
  }

  /**
   * Creates a transaction for the release of a hold.
   */
  fun releaseHoldAndCreateTransaction(
    prisonId: String,
    offenderNo: String,
    holdNumber: Number,
    clientRequest: ReleaseHoldCreateClientTransactionRequest,
  ): ReleaseHoldCreateTransactionResponse {
    val createClientReference = generateClientReference()
    val removeClientReference = generateClientReference()
    val request = ReleaseHoldCreateTransactionRequest(
      type = clientRequest.transactionType,
      removeDescription = REMOVE_HOLD_DESCRIPTION,
      createDescription = "$HOLD_DESCRIPTION for ${clientRequest.transactionType}",
      clientTransactionId = createClientReference.toClientTransactionId(),
      clientName = offenderNo,
      removeClientUniqueReference = removeClientReference.toClientUniqueReference(),
      createClientUniqueReference = createClientReference.toClientUniqueReference(),
    )

    return prisonFinanceClient.releaseHoleCreateTransaction(prisonId, offenderNo, holdNumber, request)
  }

  private fun generateClientReference(): String = UUID.randomUUID().toString()

  private fun String.toClientTransactionId(): String = take(CLIENT_TRANSACTION_ID_LENGTH)

  private fun String.toClientUniqueReference(): String = "$CLIENT_REFERENCE_PREFIX$this"
}
