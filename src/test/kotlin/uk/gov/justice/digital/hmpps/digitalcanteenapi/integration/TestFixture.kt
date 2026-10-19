package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerSearch.generated.CurrentIncentive
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerSearch.generated.IncentiveLevel
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerSearch.generated.Prisoner
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudications.generated.ActivePunishmentDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.Account
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.BalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.PrisonerSearchResponseDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

object TestFixture {
  const val PRISONER_NUMBER = "A1234BC"
  const val BOOKING_ID = "A1234BC"
  const val REFERENCE = "mark_FN"

  fun Prisoner(prisonerNumber: String = PRISONER_NUMBER) = Prisoner(
    prisonerNumber = prisonerNumber,
    prisonId = "MDI",
    prisonName = "Moorland (HMP & YOI)",
    bookNumber = "12345",
    bookingId = BOOKING_ID,
    dateOfBirth = LocalDate.of(1990, 1, 15),
    youthOffender = false,
    gender = "Female",
    currentIncentive = CurrentIncentive(
      level = IncentiveLevel(
        code = "STD",
        description = "Standard",
      ),
      dateTime = LocalDateTime.of(2020, 1, 1, 15, 0),
      nextReviewDate = LocalDate.of(2020, 2, 1),
    ),
  )

  fun prisonerSearchResponseDto(prisonerNumber: String = PRISONER_NUMBER) = PrisonerSearchResponseDto(
    prisonerNumber = prisonerNumber,
    prisonId = "MDI",
    prisonName = "Moorland (HMP & YOI)",
    bookNumber = "12345",
    bookingId = BOOKING_ID,
    dateOfBirth = LocalDate.of(1990, 1, 15),
    youthOffender = false,
    gender = "Female",
  )

  fun activePunishments() = listOf(
    ActivePunishmentDto(
      chargeNumber = "12345",
      punishmentType = ActivePunishmentDto.PunishmentType.PRIVILEGE,
      privilegeType = ActivePunishmentDto.PrivilegeType.CANTEEN,
      otherPrivilege = "none",
      duration = 5,
      measurement = ActivePunishmentDto.Measurement.DAYS,
      startDate = LocalDate.parse("2025-01-01"),
      lastDay = LocalDate.parse("2025-01-31"),
      amount = 0.1,
      stoppagePercentage = 0,
      activatedFrom = "today",
    ),
  )

  fun balanceDto() = Account(
    spends = BigDecimal(88.88),
    cash = BigDecimal(0.0),
    savings = BigDecimal(1.25),
    damageObligations = BigDecimal(0.0),
    currency = "GBP",
  )

  fun balanceResponseDto() = BalanceResponseDto(
    spendsPence = 8888,
    cashPence = 0,
    savingsPence = 125,
    damageObligationsPence = 0,
    currency = "GBP",
  )
}
