package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto

data class BtPinPhoneClientDto(
  val reference: String,
  val prisonerId: String,
  val balancePence: Int,
  val creditLimitPounds: Int,
)
