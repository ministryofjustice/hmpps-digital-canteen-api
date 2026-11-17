package uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl

data class LplProductDetails(
  val title: String?,
  val productCode: String,
  val price: Int?,
)

data class ProductMatchingResult(
  val matchedProducts: List<MatchedProduct>,
  val unmatchedProducts: List<String>,
)

data class MatchedProduct(
  val productId: String,
  val productCode: String,
  val variantId: String,
  val priceInPence: Int?,
)

data class BatchProductsRequest(
  val add: Set<String>? = null,
  val remove: Set<String>? = null,
)
