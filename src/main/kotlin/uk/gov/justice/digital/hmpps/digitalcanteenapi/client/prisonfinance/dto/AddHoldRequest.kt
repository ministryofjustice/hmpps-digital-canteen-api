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

@Suppress("ConstructorParameterNaming")
data class PaymentResult(
  val offender_no: String,
  val status: PaymentStatus,
  val transactionReference: String?,
  val holdNumber: Number,
  val errorCode: String?,
  val errorMessage: String?,
)

enum class PaymentStatus {
  AUTHORIZED,
  ERROR,
  CANCELED,
}
