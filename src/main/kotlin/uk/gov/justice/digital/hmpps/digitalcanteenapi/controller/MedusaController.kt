package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.MedusaService

@RestController
// Potentially to be replaced with new role once created
@PreAuthorize("permitAll()")
@RequestMapping("/api")
class MedusaController(
  private val medusaService: MedusaService,
) {

  @GetMapping("/test-request-to-api", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun testEndpointFromMedusa(): Map<String, String> = mapOf("message" to "successful from API")

  @GetMapping("/test-request-from-api-store", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun testMedusaEndpointStore(): Mono<MedusaDto> = medusaService.testMedusaEndpointStore()

  @GetMapping("/test-request-from-api-admin", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun testMedusaEndpointAdmin(): Mono<MedusaDto> = medusaService.testMedusaEndpointAdmin()
}
