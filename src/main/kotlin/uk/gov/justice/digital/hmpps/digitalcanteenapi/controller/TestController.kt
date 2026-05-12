package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.HealthAndMedicationClient

@RestController
@RequestMapping("/api")
class TestController(
  private val healthAndMedicationClient: HealthAndMedicationClient,
) {

  @PreAuthorize("permitAll()")
  @GetMapping("/test")
  fun testEndpoint() = healthAndMedicationClient.getPrisoner("A5441EC")
}
