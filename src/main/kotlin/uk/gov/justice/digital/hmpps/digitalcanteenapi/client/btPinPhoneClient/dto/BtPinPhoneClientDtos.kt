package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto

// Auth token
data class BtTokenRequest(
  val clientId: String,
  val clientSecret: String,
)

data class BtTokenResponse(
  val accessToken: String,
  val tokenType: String,
  val expiresIn: Long,
)

// Balance
data class BtPinPhoneBalanceRequest(
  val reference: String,
  val prisonerId: String,
)

data class BtPinPhoneBalanceResponseDto(
  val reference: String,
  val prisonerId: String,
  val balancePence: Int,
  val creditLimitPence: Int,
)

// contacts
data class BtPinPhoneContactsRequest(
  val reference: String,
  val prisonerId: String,
)

data class BtPinPhoneContactsResponseDto(
  val reference: String,
  val prisonerId: String,
  val controlledNumbers: List<ControlledNumber>,
)

data class ControlledNumber(
  val id: Int,
  val name: String,
  val phoneNumber: String,
  val controlStatus: Boolean,
  val callAllowed: Boolean,
  val legal: Boolean,
  val allowMonitor: Boolean,
  val alert: Boolean,
  val override: Boolean,
  val relationshipId: Int,
)

// Buy credit
data class BtPinPhoneBuyCreditRequest(
  val reference: String,
  val prisonerId: String,
  val amountPence: Int,
  val type: Int,
)
