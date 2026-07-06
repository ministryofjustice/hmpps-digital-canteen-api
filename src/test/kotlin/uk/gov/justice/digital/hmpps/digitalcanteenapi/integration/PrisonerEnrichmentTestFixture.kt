package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneClientDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient.dto.Punishment
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerincentivesclient.dto.PrisonerIncentivesDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.dto.PrisonerSearchDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.BalanceDto
import java.time.LocalDate
import kotlin.String

object PrisonerEnrichmentTestFixture {
  const val PRISONER_NUMBER = "A1234BC"
  const val BOOKING_ID = "A1234BC"

  fun prisonerSearchDto(prisonerNumber: String = PRISONER_NUMBER) = PrisonerSearchDto(
    prisonerNumber = prisonerNumber,
    prisonId = "MDI",
    prisonName = "Moorland (HMP & YOI)",
    bookNumber = "12345",
    bookingId = BOOKING_ID,
    dateOfBirth = LocalDate.of(1990, 1, 15),
    youthOffender = false,
    gender = "Female",
  )

  fun prisonerIncentivesDto(prisonerNumber: String = PRISONER_NUMBER) = PrisonerIncentivesDto(
    id = 12345L,
    iepCode = "STD",
    iepLevel = "Standard",
    prisonerNumber = prisonerNumber,
    iepDate = "2025-01-15",
    iepTime = "14:30:00",
  )

  fun activePunishments() = listOf(
    Punishment(
      chargeNumber = "12345",
      punishmentType = "PRIVILEGE",
      privilegeType = "CANTEEN",
      otherPrivilegeType = "none",
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

  fun btPinPhoneDto(prisonerNumber: String = PRISONER_NUMBER) = BtPinPhoneClientDto(
    reference = "test reference",
    prisonerId = prisonerNumber,
    balancePence = 12.88,
    creditLimitPounds = 50.00,
  )
}
