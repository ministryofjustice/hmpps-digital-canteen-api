package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class MedusaDto(
  val status: String,
)

data class Order(
  val id: String,
)

data class CreateCartRequest(
  var prisonId: String,
  var offenderNo: String,
  var firstName: String,
  var lastName: String,
)

data class MedusaCreateCartRequest(
  val metadata: CartMetadata,
)

data class CartMetadata(
  val prison_id: String,
  val offender_no: String,
  val first_name: String,
  val last_name: String,
)

data class MedusaCreateCartResponse(
  val cart: MedusaCart,
)

data class MedusaCart(
  val id: String,

  @JsonProperty("region_id")
  val regionId: String,

  @JsonProperty("customer_id")
  val customerId: String?,

  @JsonProperty("currency_code")
  val currencyCode: String,
)
