package uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment.dto

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneBalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.dto.IncentivesDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.dto.PrisonerSearchDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.BalanceDto
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime

data class PrisonerSearchResponseDto(
  val prisonerNumber: String,
  val prisonId: String,
  val prisonName: String,
  val bookNumber: String,
  val bookingId: String,
  val dateOfBirth: LocalDate,
  val youthOffender: Boolean,
  val gender: String,
)

data class PrisonerIncentivesResponseDto(
  val code: String,
  val description: String,
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

fun BtPinPhoneBalanceResponseDto.toBtPinPhoneResponseDto() = BtPinPhoneResponseDto(
  reference = reference,
  prisonerId = prisonerId,
  balancePence = balancePence,
  creditLimitPence = creditLimitPence,
  isFn = reference.endsWith("_FN"),
)

fun PrisonerSearchDto.toPrisonerSearchResponseDto() = PrisonerSearchResponseDto(
  prisonerNumber = prisonerNumber,
  prisonId = prisonId,
  prisonName = prisonName,
  bookNumber = bookNumber,
  bookingId = bookingId,
  dateOfBirth = dateOfBirth,
  youthOffender = youthOffender,
  gender = gender,
)

fun IncentivesDto.toPrisonerIncentiveResponseDto() = PrisonerIncentivesResponseDto(
  code = level.code,
  description = level.description,
  dateTime = dateTime,
  nextReviewDate = nextReviewDate,
)

fun BalanceDto.toBalanceResponseDto() = BalanceResponseDto(
  spendsPence = spends.toPence(),
  cashPence = cash.toPence(),
  savingsPence = savings.toPence(),
  damageObligationsPence = damageObligations.toPence(),
  currency = currency,
)

@Suppress("MagicNumber")
private fun Double.toPence(): Long = BigDecimal(this.toString())
  .multiply(BigDecimal(100))
  .setScale(0, RoundingMode.HALF_UP)
  .longValueExact()
