package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.OpaService
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.dto.Decision

@RestController
@RequestMapping("/api")
class OpaRuleTestController(
  private val opaService: OpaService
) {

  @PreAuthorize("permitAll()")
  @GetMapping("/rules-opa/{prisonerNumber}/{creditRequested}")
  fun getProduct(@PathVariable prisonerNumber: String, @PathVariable creditRequested: Int,
  ): Mono<Decision> = opaService.evaluatePinPhone(prisonerNumber, creditRequested)
}