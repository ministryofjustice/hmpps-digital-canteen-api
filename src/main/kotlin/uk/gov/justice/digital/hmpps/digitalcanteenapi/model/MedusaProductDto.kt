package uk.gov.justice.digital.hmpps.digitalcanteenapi.model

import java.util.Collections.emptyList

data class ProductListResponse(
  val products: List<MedusaProduct>,
  val count: Int? = null,
  val offset: Int? = null,
  val limit: Int? = null,
)

data class MedusaProduct(
  val id: String,
  val handle: String,
  val title: String,
  val subtitle: String?,
  val description: String?,
  val variants: List<MedusaVariant> = emptyList(),
  val metadata: ProductMetadata?,
)

data class MedusaVariant(
  val id: String,
)

data class ProductMetadata(
  val lplOrderingCode: String?,
  val eanBarcode: String?,
  val selectionCategory: String?,
)
