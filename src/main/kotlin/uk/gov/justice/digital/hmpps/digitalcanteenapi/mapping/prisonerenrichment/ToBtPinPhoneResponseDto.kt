package uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceResponse

fun BtPinPhoneBalanceResponse.toBtPinPhoneResponseDto() = BtPinPhoneResponseDto(
  reference = reference,
  prisonerId = prisonerId,
  balancePence = balancePence,
  creditLimitPence = creditLimitPence,
  isFn = reference.endsWith("_FN"),
)

data class BtPinPhoneResponseDto(
  val reference: String,
  val prisonerId: String,
  val balancePence: Int,
  val creditLimitPence: Int,
  val isFn: Boolean,
)
