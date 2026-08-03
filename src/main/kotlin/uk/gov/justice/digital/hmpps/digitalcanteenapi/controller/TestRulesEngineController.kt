package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.ProductRestrictionService

@RestController
@RequestMapping("/api")
class TestRulesEngineController(private val productRestrictionService: ProductRestrictionService) {


  @PreAuthorize("permitAll()")
  @GetMapping("/rules-evaluate/{prisonerNumber}")
  fun getProduct(@PathVariable prisonerNumber: String,
  ): Mono<ProductRestrictionService.ProductRestrictionResponse> =
    productRestrictionService.getProductRestrictionInfo(prisonerNumber)
}
