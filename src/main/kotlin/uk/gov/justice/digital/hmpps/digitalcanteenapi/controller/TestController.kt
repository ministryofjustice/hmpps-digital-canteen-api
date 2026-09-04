package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneTestSupportClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtRelationshipsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateAccountRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateAccountResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateControlledNumberRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateControlledNumberResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreditAccountRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreditAccountResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.productenrichment.dto.ProductDetailsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.ProductEnrichmentInfoService

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ROLE_PIN_PHONE_CREDIT_API')")
@Profile("dev", "test")
class TestController(
  private val productEnrichmentInfoService: ProductEnrichmentInfoService,
  private val btPinPhoneTestSupportClient: BtPinPhoneTestSupportClient,
) {

  // Product endpoints
  @GetMapping("/product/{ean}")
  fun getProduct(
    @PathVariable ean: String,
  ): Mono<ProductDetailsResponse> = productEnrichmentInfoService.getProductEnrichmentDetails(ean)

  // BT endpoints
  @Operation(summary = "Get BT auth token")
  @GetMapping("/bt-auth-test")
  fun testBtAuth() = btPinPhoneTestSupportClient.getBtToken()

  @Operation(summary = "Get balances for BT account")
  @GetMapping("/get-balances-test/{prisonerId}/{reference}")
  fun testBt(
    @PathVariable prisonerId: String,
    @PathVariable reference: String,
  ): Mono<BtPinPhoneBalanceResponse> = btPinPhoneTestSupportClient.getPrisonerBalance(
    BtPinPhoneBalanceRequest(
      reference = reference,
      prisonerId = prisonerId,
    ),
  )

  @Operation(summary = "Create BT account")
  @PutMapping("/bt-test/account-test")
  fun createBtAccount(
    @RequestBody request: CreateAccountRequest,
  ): Mono<CreateAccountResponse> = btPinPhoneTestSupportClient.createAccount(request)

  @Operation(summary = "Add controlled number to BT account")
  @PutMapping("/bt-test/controlled-number-test")
  fun createBtControlledNumber(
    @RequestBody request: CreateControlledNumberRequest,
  ): Mono<CreateControlledNumberResponse> = btPinPhoneTestSupportClient.createControlledNumber(request)

  @Operation(summary = "Get relationship types for BT")
  @PostMapping("/bt-test/relationships-test")
  fun getBtRelationships(): Mono<BtRelationshipsResponse> = btPinPhoneTestSupportClient.getRelationships()

  @Operation(summary = "Add account credit to BT account")
  @PostMapping("/bt-test/account-credit-test")
  fun creditBtAccount(
    @RequestBody request: CreditAccountRequest,
  ): Mono<CreditAccountResponse> = btPinPhoneTestSupportClient.accountCredit(request)
}
