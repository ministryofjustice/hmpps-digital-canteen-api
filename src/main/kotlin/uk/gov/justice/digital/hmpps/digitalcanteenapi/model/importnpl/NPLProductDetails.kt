package uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importnpl

import java.math.BigDecimal

data class NplProduct(
  val icCode: String,
  val lplOrderingCode: String?,
  val description: String?,
  val shortDescription: String?,
  val eanBarcode: String?,
  val pricePence: BigDecimal?,
  val selectionCategories: String?,
)

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
  val metadata: ProductMetadata?,
)

data class ProductMetadata(
  val lplOrderingCode: String?,
  val eanBarcode: String?,
  val selectionCategory: String?,
)
