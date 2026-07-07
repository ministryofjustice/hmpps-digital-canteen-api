package uk.gov.justice.digital.hmpps.digitalcanteenapi.service.dto

import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.PinPhonePrisonerEnrichmentService

data class OpaInput(
  val prisoner: PinPhonePrisonerEnrichmentService.EnrichedPinPhonePrisonerDto,
  val creditRequested: Int,
)

data class Decision(
  val deny_purchase: Boolean,
  val show_warnings: List<String>,
)

data class OpaResponse(
  val result: List<Decision>
)