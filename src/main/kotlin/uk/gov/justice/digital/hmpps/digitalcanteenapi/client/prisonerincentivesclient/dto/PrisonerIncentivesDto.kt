package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerincentivesclient.dto

data class PrisonerIncentivesDto(
  val id: Long,
  val iepCode: String,
  val iepLevel: String,
  val prisonerNumber: String,
  val iepDate: String,
  val iepTime: String,
)
