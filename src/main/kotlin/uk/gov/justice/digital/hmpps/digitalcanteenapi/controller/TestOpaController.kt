package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.ProductDecision
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.OpaService

@RestController
@RequestMapping("/api")
class OpaRuleTestController(
  private val opaService: OpaService
) {

  @PreAuthorize("permitAll()")
  @GetMapping("/rules-opa/{prisonerNumber}")
  fun getProduct(@PathVariable prisonerNumber: String,
  ): ProductDecision = opaService.evaluateProducts(prisonerNumber)
}

