package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.ProductEnrichmentInfoService
import uk.gov.justice.digital.hmpps.digitalcanteenapi.dto.ProductDetailsResponse

@RestController
@RequestMapping("/api")
class TestController(private val productEnrichmentDetailsService: ProductEnrichmentInfoService) {

  @Suppress("FunctionOnlyReturningConstant")
  @PreAuthorize("permitAll()")
  @GetMapping("/test")
  fun testEndpoint(): String = "test"

  @PreAuthorize("permitAll()")
  @GetMapping("/product/{ean}")
  fun getProduct(@PathVariable ean: String): Mono<ProductDetailsResponse>
  = productEnrichmentDetailsService.getProductEnrichmentDetails(ean)
}
