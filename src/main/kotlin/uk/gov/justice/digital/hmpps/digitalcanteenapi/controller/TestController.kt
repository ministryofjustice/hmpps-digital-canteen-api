package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.productenrichment.dto.ProductDetailsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.ProductEnrichmentInfoService

@RestController
@RequestMapping("/api")
class TestController(
  private val productEnrichmentInfoService: ProductEnrichmentInfoService,
  private val btPinPhoneClient: BtPinPhoneClient,
) {

  @Suppress("FunctionOnlyReturningConstant")
  @PreAuthorize("permitAll()")
  @GetMapping("/test")
  fun testEndpoint(): String = "test"

  @PreAuthorize("permitAll()")
  @GetMapping("/product/{ean}")
  fun getProduct(
    @PathVariable ean: String,
  ): Mono<ProductDetailsResponse> = productEnrichmentInfoService.getProductEnrichmentDetails(ean)

  @PreAuthorize("permitAll()")
  @GetMapping("/test-bt-auth")
  fun testBtAuth() = btPinPhoneClient.getBtToken()

  @PreAuthorize("permitAll()")
  @GetMapping("/test-bt/{prisonerId}/{reference}")
  fun testBt(
    @PathVariable prisonerId: String,
    @PathVariable reference: String,
  ): Mono<BtPinPhoneBalanceResponse> = btPinPhoneClient.getPrisonerBalance(
    BtPinPhoneBalanceRequest(
      reference = reference,
      prisonerId = prisonerId,
    ),
  )
}
