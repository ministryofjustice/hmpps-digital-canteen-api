package uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl

import com.fasterxml.jackson.annotation.JsonProperty

data class PriceListMetadata(
  val prisonId: String?,
)

data class PriceList(
  val id: String,
  val title: String,
  val type: String,
  val metadata: PriceListMetadata? = null,
)

data class PriceListResponse(
  @JsonProperty("price_lists")
  val priceLists: List<PriceList>,
)

data class CreatePriceListDto(
  val title: String,
  val description: String?,
  val type: String = "override",
  val status: String? = null,
  val metadata: PriceListMetadata? = null,
  @JsonProperty("prices")
  val prices: List<PriceListPrice>,
)

data class PriceListPrice(
  @JsonProperty("variant_id")
  val variantId: String,
  val amount: Int,
  @JsonProperty("currency_code")
  val currencyCode: String,
  val rules: Map<String, String>,
)

data class CreatePriceListResponse(
  @JsonProperty("price_list")
  val priceList: PriceList,
)
