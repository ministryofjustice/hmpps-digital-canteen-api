package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.HealthAndMedicationClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerAdjudicationsClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerIncentivesClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.PrisonerEnrichmentService

@RestController
@RequestMapping("/api")
class TestController(
  private val healthAndMedicationClient: HealthAndMedicationClient,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonerAdjudicationsClient: PrisonerAdjudicationsClient,
  private val prisonerIncentivesClient: PrisonerIncentivesClient,
  private val prisonerEnrichmentService: PrisonerEnrichmentService

  ) {

  @PreAuthorize("permitAll()")
  @GetMapping("/test")
  //fun testEndpoint1() = healthAndMedicationClient.getPrisoner("G5661UP")
  //fun testEndpoint2() = prisonerSearchClient.getPrisoner("A5441EC")
  //fun testEndpoint3() = prisonerAdjudicationsClient.getPrisonerHasAdjudication("G8478VW")
  //fun testEndpoint4() = prisonerAdjudicationsClient.getPrisonerAdjudication("G8478VW")
  //fun testEndpoint5() = prisonerIncentivesClient.getPrisoner("G5661UP")

  fun testEndpoint6() = prisonerEnrichmentService.getEnrichedPrisoner("G5661UP")
}
