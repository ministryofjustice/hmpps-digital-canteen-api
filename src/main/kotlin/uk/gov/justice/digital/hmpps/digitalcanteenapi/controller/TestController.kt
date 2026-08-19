package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

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
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneTestSupportClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtRelationshipsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateAccountRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateAccountResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateControlledNumberRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.CreateControlledNumberResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.productenrichment.dto.ProductDetailsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.PrisonerEnrichmentRulesEnginePoc
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.ProductEnrichmentInfoService

@RestController
@RequestMapping("/api")
@Profile("dev", "test")
class TestController(
  private val productEnrichmentInfoService: ProductEnrichmentInfoService,
  private val btPinPhoneTestSupportClient: BtPinPhoneTestSupportClient,
  private val btPinPhoneClient: BtPinPhoneClient,
  private val prisonerEnrichmentRulesEnginePoc: PrisonerEnrichmentRulesEnginePoc,
) {

  @Suppress("FunctionOnlyReturningConstant")
  @PreAuthorize("permitAll()")
  @GetMapping("/test")
  fun testEndpoint(): String = "test"

  // Product endpoints
  @PreAuthorize("permitAll()")
  @GetMapping("/product/{ean}")
  fun getProduct(
    @PathVariable ean: String,
  ): Mono<ProductDetailsResponse> = productEnrichmentInfoService.getProductEnrichmentDetails(ean)

  // BT endpoints
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

  @PreAuthorize("permitAll()")
  @PostMapping("/bt-test/relationships")
  fun getBtRelationships(): Mono<BtRelationshipsResponse> = btPinPhoneTestSupportClient.getRelationships()

  // Prisoner Enrichment endpoints
  @PreAuthorize("permitAll()")
  @GetMapping("/prisoner-enrichment-poc/{prisonerNumber}")
  fun getEnrichedPrisoner(
    @PathVariable prisonerNumber: String,
  ) = prisonerEnrichmentRulesEnginePoc.getEnrichedPrisoner(prisonerNumber)
}
