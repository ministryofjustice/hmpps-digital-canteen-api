package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto

data class AddHoldRequest(
  val description: String,
  val amount: Number,
  val clientTransactionId: String,
  val clientName: String,
  val clientUniqueReference: String,
)

data class AddHoldResponse(
  val holdNumber: Number,
)

data class AddHoldClientRequest(
  val amount: Number,
)
