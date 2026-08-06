package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneTestSupportClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateAccountRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateAccountResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateControlledNumberRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateControlledNumberResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.productenrichment.dto.ProductDetailsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.ProductEnrichmentInfoService

@RestController
@RequestMapping("/api")
class TestController(
  private val productEnrichmentInfoService: ProductEnrichmentInfoService,
  private val btPinPhoneTestSupportClient: BtPinPhoneTestSupportClient,
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
  @PutMapping("/bt-test/account")
  fun createBtAccount(
    @RequestBody request: CreateAccountRequest,
  ): Mono<CreateAccountResponse> = btPinPhoneTestSupportClient.createAccount(request)

  @PreAuthorize("permitAll()")
  @PutMapping("/bt-test/controlled-number")
  fun createBtControlledNumber(
    @RequestBody request: CreateControlledNumberRequest,
  ): Mono<CreateControlledNumberResponse> = btPinPhoneTestSupportClient.createControlledNumber(request)
}
