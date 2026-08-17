package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.AddHoldTransaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.HoldDetails
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.ReleaseHoldAndCreateTransaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.Transaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.PrisonFinanceService

@RestController
@RequestMapping("/api/finance")
@PreAuthorize("permitAll()")
class PrisonFinanceController(
  private val prisonFinanceService: PrisonFinanceService,
) {
  @Suppress("FunctionOnlyReturningConstant", "FunctionExpressionBody")
  @PostMapping("/prisons/{prisonId}/offenders/{offenderNo}/addHold")
  fun addHold(
    @PathVariable prisonId: String,
    @PathVariable offenderNo: String,
    @RequestBody request: AddHoldTransaction,
  ): HoldDetails = prisonFinanceService.addHold(prisonId, offenderNo, request.amount)

  @PostMapping("/prisons/{prisonId}/offenders/{offenderNo}/releaseHold/{holdNumber}")
  fun releaseHold(
    @PathVariable prisonId: String,
    @PathVariable offenderNo: String,
    @PathVariable holdNumber: Number,
  ): ResponseEntity<Void> = prisonFinanceService.releaseHold(prisonId, offenderNo, holdNumber)

  @Suppress("MaxLineLength")
  @PostMapping("/prisons/{prisonId}/offenders/{offenderNo}/releaseHoldCreateTransaction/{holdNumber}")
  fun releaseHoldAndCreateTransaction(
    @PathVariable prisonId: String,
    @PathVariable offenderNo: String,
    @PathVariable holdNumber: Number,
    @RequestBody request: ReleaseHoldAndCreateTransaction,
  ): Transaction = prisonFinanceService.releaseHoldAndCreateTransaction(prisonId, offenderNo, holdNumber, request.type)
}
