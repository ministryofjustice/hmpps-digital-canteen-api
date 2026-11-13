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
