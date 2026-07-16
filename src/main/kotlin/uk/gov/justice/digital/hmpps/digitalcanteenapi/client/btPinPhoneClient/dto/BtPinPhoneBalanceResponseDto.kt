package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto

import io.swagger.v3.oas.models.security.SecurityScheme

data class BtPinPhoneBalanceResponseDto(
  val reference: String,
  val prisonerId: String,
  val balancePence: Int,
  val creditLimitPence: Int,
)

data class BtPinPhoneBuyCreditRequest(
   val reference: String,
   val prisonerId: String,
   val amountPence: Int,
   val type: Int

)
