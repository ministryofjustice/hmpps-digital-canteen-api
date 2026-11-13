package uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl

import com.fasterxml.jackson.annotation.JsonProperty

data class MedusaRegionsResponse(
  val regions: List<MedusaRegion>,
)

data class MedusaRegion(
  val id: String,
  val name: String,
  @JsonProperty("currency_code")
  val currencyCode: String,
  val metadata: Map<String, Any>,
)
