package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient.dto.AdjudicationsPunishmentDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.dto.IncentivesDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.dto.IncentivesLevelDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.dto.PrisonerSearchDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.BalanceDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment.dto.BalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment.dto.BtPinPhoneResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment.dto.PrisonerIncentivesResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment.dto.PrisonerSearchResponseDto
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.String

object PinPhonePrisonerEnrichmentTestFixture {
  const val PRISONER_NUMBER = "A1234BC"
  const val BOOKING_ID = "A1234BC"
  const val REFERENCE = "mark_FN"

  fun prisonerSearchDto(prisonerNumber: String = PRISONER_NUMBER) = PrisonerSearchDto(
    prisonerNumber = prisonerNumber,
    prisonId = "MDI",
    prisonName = "Moorland (HMP & YOI)",
    bookNumber = "12345",
    bookingId = BOOKING_ID,
    dateOfBirth = LocalDate.of(1990, 1, 15),
    youthOffender = false,
    gender = "Female",
    currentIncentive = IncentivesDto(
      level = IncentivesLevelDto(
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

  fun prisonerSearchIncentiveResponseDto() = PrisonerIncentivesResponseDto(
    code = "STD",
    description = "Standard",
    dateTime = LocalDateTime.of(2020, 1, 1, 15, 0),
    nextReviewDate = LocalDate.of(2020, 2, 1),
  )

  fun activePunishments() = listOf(
    AdjudicationsPunishmentDto(
      chargeNumber = "12345",
      punishmentType = "PRIVILEGE",
      privilegeType = "CANTEEN",
      otherPrivilege = "none",
      duration = 5,
      measurement = "DAYS",
      startDate = LocalDate.parse("2025-01-01"),
      lastDay = LocalDate.parse("2025-01-31"),
      amount = 0.1,
      stoppagePercentage = 0,
      activatedFrom = "today",
    ),
  )

  fun balanceDto() = BalanceDto(
    spends = 88.88,
    cash = 0.0,
    savings = 1.25,
    damageObligations = 0.0,
    currency = "GBP",
  )

  fun balanceResponseDto() = BalanceResponseDto(
    spendsPence = 8888,
    cashPence = 0,
    savingsPence = 125,
    damageObligationsPence = 0,
    currency = "GBP",
  )

  fun btPinPhoneDto(prisonerNumber: String = PRISONER_NUMBER) = BtPinPhoneBalanceResponse(
    reference = REFERENCE,
    prisonerId = prisonerNumber,
    balancePence = 1288,
    creditLimitPence = 5000,
  )

  fun btPinPhoneResponseDto(prisonerNumber: String = PRISONER_NUMBER) = BtPinPhoneResponseDto(
    reference = REFERENCE,
    prisonerId = prisonerNumber,
    balancePence = 1288,
    creditLimitPence = 5000,
    isFn = REFERENCE.endsWith("_FN"),
  )
}
