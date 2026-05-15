package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.PrisonerEnrichmentService

@RestController
@RequestMapping("/api")
class TestPrisonerEnrichmentController(
  private val prisonerEnrichmentService: PrisonerEnrichmentService,
) {

  @PreAuthorize("permitAll()")
  @GetMapping("/prisoner-enrichment/{prisonerNumber}")
  @Suppress("MaxLineLength")
  fun testEndpoint6(@PathVariable prisonerNumber: String) = prisonerEnrichmentService.getEnrichedPrisoner(prisonerNumber)
}
