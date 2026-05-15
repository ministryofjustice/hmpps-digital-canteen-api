package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto

import java.time.LocalDate

data class PrisonerSearchDto(
  val prisonerNumber: String,
  val prisonId: String? = null,
  val prisonName: String? = null,
  val bookNumber: String? = null,
  val bookingId: String? = null,
  val dateOfBirth: LocalDate? = null,
  val youthOffender: Boolean? = null,
  val gender: String? = null,
)
