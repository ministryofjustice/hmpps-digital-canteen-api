package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto

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
