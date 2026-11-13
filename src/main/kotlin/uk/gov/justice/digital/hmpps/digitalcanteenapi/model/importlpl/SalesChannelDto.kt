package uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl

import com.fasterxml.jackson.annotation.JsonProperty

data class SalesChannelsResponse(
  @JsonProperty("sales_channels")
  val salesChannels: List<MedusaSalesChannel>,
)

data class MedusaSalesChannel(
  val id: String,
  val name: String,
  val description: String?,
)

data class SalesChannelProductsResponse(
  val products: List<SalesChannelProduct>,
  val count: Int,
  val offset: Int,
  val limit: Int,
)

data class SalesChannelProduct(
  val id: String,
  val title: String? = null,
)
