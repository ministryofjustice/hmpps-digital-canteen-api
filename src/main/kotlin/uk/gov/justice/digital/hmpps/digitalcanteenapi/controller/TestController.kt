package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.AddHoldTransaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.HoldDetails
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.ReleaseHoldAndCreateTransaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.Transaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.productenrichment.dto.ProductDetailsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.PrisonFinanceService
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.ProductEnrichmentInfoService

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ROLE_PIN_PHONE_CREDIT_API')")
@Profile("dev", "test")
class TestController(
  private val productEnrichmentInfoService: ProductEnrichmentInfoService,
  private val prisonFinanceService: PrisonFinanceService,
) {

  // Product endpoints
  @GetMapping("/product/{ean}")
  fun getProduct(
    @PathVariable ean: String,
  ): Mono<ProductDetailsResponse> = productEnrichmentInfoService.getProductEnrichmentDetails(ean)

  // prison finance endpoints
  @PostMapping("/finance/prisons/{prisonId}/offenders/{offenderNo}/addHold")
  fun addHold(
    @PathVariable prisonId: String,
    @PathVariable offenderNo: String,
    @RequestBody request: AddHoldTransaction,
  ): HoldDetails = prisonFinanceService.addHold(prisonId, offenderNo, request.amount)

  @PostMapping("/finance/prisons/{prisonId}/offenders/{offenderNo}/releaseHold/{holdNumber}")
  fun releaseHold(
    @PathVariable prisonId: String,
    @PathVariable offenderNo: String,
    @PathVariable holdNumber: Number,
  ): ResponseEntity<Void> = prisonFinanceService.releaseHold(prisonId, offenderNo, holdNumber)

  @PostMapping("/finance/prisons/{prisonId}/offenders/{offenderNo}/releaseHoldCreateTransaction/{holdNumber}")
  fun releaseHoldAndCreateTransaction(
    @PathVariable prisonId: String,
    @PathVariable offenderNo: String,
    @PathVariable holdNumber: Number,
    @RequestBody request: ReleaseHoldAndCreateTransaction,
  ): Transaction = prisonFinanceService.releaseHoldAndCreateTransaction(prisonId, offenderNo, holdNumber, request.type)
}
