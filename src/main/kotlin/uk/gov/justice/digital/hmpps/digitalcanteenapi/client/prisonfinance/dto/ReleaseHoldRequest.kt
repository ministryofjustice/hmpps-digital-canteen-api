package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto

data class ReleaseHoldRequest(
  val description: String,
  val clientTransactionId: String,
  val clientName: String,
  val clientUniqueReference: String,
)

data class ReleaseHoldCreateTransactionRequest(
  val type: String,
  val removeDescription: String,
  val createDescription: String,
  val clientTransactionId: String,
  val clientName: String,
  val removeClientUniqueReference: String,
  val createClientUniqueReference: String,
)

data class ReleaseHoldCreateClientTransactionRequest(
  val transactionType: String,
)

data class ReleaseHoldCreateTransactionResponse(
  val id: String,
)
