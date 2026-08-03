package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import com.deliveredtechnologies.rulebook.FactMap
import com.deliveredtechnologies.rulebook.model.runner.RuleBookRunner

import org.springframework.stereotype.Service

@Service
class RuleBookRestrictionService(
  private val prisonerEnrichmentService: PrisonerEnrichmentService,
  private val productEnrichmentInfoService: ProductEnrichmentInfoService
) {

  fun checkRestrictions(prisonerNumber: String): RestrictionResult {

    val prisoner = prisonerEnrichmentService.getEnrichedPrisoner(prisonerNumber)
      .map { it.copy(hasActiveAdjudications = false) }
      .block()!!

    val product = productEnrichmentInfoService.getProductEnrichmentDetails("3017620422003")
      .block()!!

    val facts = FactMap<Any>()
    facts.setValue("prisoner", prisoner)
    facts.setValue("product", product)

    val ruleBook = RuleBookRunner("uk.gov.justice.digital.hmpps.digitalcanteenapi.service")
    ruleBook.run(facts)

    return ruleBook.result
      .map { it.value as RestrictionResult }
      .orElse(RestrictionResult(restricted = false, restrictionType = null, reason = null))
  }
}