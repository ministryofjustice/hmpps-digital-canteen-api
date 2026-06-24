package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import com.deliveredtechnologies.rulebook.RuleState
import com.deliveredtechnologies.rulebook.annotation.*

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.productenrichment.dto.ProductDetailsResponse

@Rule(order = 1, name = "AdjudicationRestrictionRule")
class AdjudicationRestrictionRule {

  @Given("prisoner")
  private lateinit var prisoner: PrisonerEnrichmentService.EnrichedPrisonerDto

  @Given("product")
  private lateinit var product: ProductDetailsResponse

  @Result
  private var result: RestrictionResult = RestrictionResult(
    restricted = false,
    restrictionType = null,
    reason = null
  )

  @When
  fun isApplicable(): Boolean {
    return prisoner.hasActiveAdjudications == true
  }

  @Then
  fun execute(): RuleState {
    result = RestrictionResult(
      restricted = true,
      restrictionType = "ADJUDICATION",
      reason = "Prisoner has active adjudications"
    )
    return RuleState.BREAK
  }
}