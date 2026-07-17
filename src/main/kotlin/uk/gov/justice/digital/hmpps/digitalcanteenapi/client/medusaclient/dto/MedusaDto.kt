package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto

data class MedusaDto(
  val status: String,
)

data class CompleteCartResponse(
  val order: Order? = null,
  val message: String? = null,
  val code: String? = null,
)

data class Order(
  val id: String,
)
