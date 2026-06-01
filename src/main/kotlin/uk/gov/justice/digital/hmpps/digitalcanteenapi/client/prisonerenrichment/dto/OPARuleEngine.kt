package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.productenrichment.dto.ProductDetailsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.PrisonerEnrichmentService

data class  OpaInput(
  val prisoner: PrisonerEnrichmentService.EnrichedPrisonerDto,
  val product: ProductDetailsResponse
)

data class ProductDecision(
  val productId: String,
  val hide: Boolean,
  val show_warnings: List<String>,
  val labels: List<String>
)

data class OpaResponse(
  val result: List<ProductDecision>
)
