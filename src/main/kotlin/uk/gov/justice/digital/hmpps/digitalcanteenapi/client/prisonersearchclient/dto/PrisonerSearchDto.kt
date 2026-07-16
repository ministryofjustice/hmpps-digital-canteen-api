package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class PrisonerSearchDto(
  val prisonerNumber: String,
  val prisonId: String,
  val prisonName: String,
  val bookNumber: String,
  val bookingId: String,
  val dateOfBirth: LocalDate,
  val youthOffender: Boolean,
  val gender: String,
  val currentIncentive: IncentivesDto,
)

data class IncentivesDto(
  val level: IncentivesLevelDto,
  val dateTime: LocalDateTime,
  val nextReviewDate: LocalDate,
)

data class IncentivesLevelDto(
  val code: String,
  val description: String,
)
