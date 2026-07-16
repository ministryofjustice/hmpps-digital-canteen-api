package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto

data class BalanceDto(
  val spends: Double,
  val cash: Double,
  val savings: Double,
  val damageObligations: Double,
  val currency: String,
)
