package uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerSearch.generated.CurrentIncentive
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerSearch.generated.Prisoner
import java.time.LocalDate
import java.time.LocalDateTime

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
