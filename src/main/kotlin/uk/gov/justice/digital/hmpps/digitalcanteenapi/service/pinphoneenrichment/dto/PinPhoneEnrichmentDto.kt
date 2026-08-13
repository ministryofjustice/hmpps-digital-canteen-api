package uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment.dto

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerSearch.generated.CurrentIncentive
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerSearch.generated.Prisoner
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.Account
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime

data class PrisonerSearchResponseDto(
  val prisonerNumber: String,
  val prisonId: String?,
  val prisonName: String?,
  val bookNumber: String?,
  val bookingId: String?,
  val dateOfBirth: LocalDate?,
  val youthOffender: Boolean?,
  val gender: String?,
)

data class PrisonerIncentivesResponseDto(
  val code: String?,
  val description: String?,
  val dateTime: LocalDateTime,
  val nextReviewDate: LocalDate,
)

data class BalanceResponseDto(
  val spendsPence: Long,
  val cashPence: Long,
  val savingsPence: Long,
  val damageObligationsPence: Long,
  val currency: String,
)

data class BtPinPhoneResponseDto(
  val reference: String,
  val prisonerId: String,
  val balancePence: Int,
  val creditLimitPence: Int,
  val isFn: Boolean,
)

fun BtPinPhoneBalanceResponse.toBtPinPhoneResponseDto() = BtPinPhoneResponseDto(
  reference = reference,
  prisonerId = prisonerId,
  balancePence = balancePence,
  creditLimitPence = creditLimitPence,
  isFn = reference.endsWith("_FN"),
)

fun Prisoner.toPrisonerSearchResponseDto() = PrisonerSearchResponseDto(
  prisonerNumber = prisonerNumber,
  prisonId = prisonId,
  prisonName = prisonName,
  bookNumber = bookNumber,
  bookingId = bookingId,
  dateOfBirth = dateOfBirth,
  youthOffender = youthOffender,
  gender = gender,
)

fun CurrentIncentive.toPrisonerIncentiveResponseDto() = PrisonerIncentivesResponseDto(
  code = level?.code,
  description = level?.description,
  dateTime = dateTime,
  nextReviewDate = nextReviewDate,
)

fun Account.toBalanceResponseDto() = BalanceResponseDto(
  spendsPence = spends.toPence(),
  cashPence = cash.toPence(),
  savingsPence = savings.toPence(),
  damageObligationsPence = damageObligations.toPence(),
  currency = currency,
)

@Suppress("MagicNumber")
private fun BigDecimal.toPence(): Long = this
  .multiply(BigDecimal(100))
  .setScale(0, RoundingMode.HALF_UP)
  .longValueExact()
