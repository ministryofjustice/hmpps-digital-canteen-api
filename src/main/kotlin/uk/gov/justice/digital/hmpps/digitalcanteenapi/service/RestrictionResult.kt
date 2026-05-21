package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

data class RestrictionResult(
  val restricted: Boolean?,
  val restrictionType: String?,
  val reason: String?,
)