package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.RestrictionResult

import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.RuleBookRestrictionService

@RestController
@RequestMapping("/api")
class RuleBookTestController(
  private val ruleBookService: RuleBookRestrictionService
) {

  @PreAuthorize("permitAll()")
  @GetMapping("/rules-book/{prisonerNumber}")
  fun getProduct(@PathVariable prisonerNumber: String,
  ): RestrictionResult = ruleBookService.checkRestrictions(prisonerNumber)
}

