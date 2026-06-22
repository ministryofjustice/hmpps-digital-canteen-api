package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldClientRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateClientTransactionRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateTransactionResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.PrisonFinanceService

@RestController
@RequestMapping("/api/finance")
class PrisonFinanceController(
  private val prisonFinanceService: PrisonFinanceService,
) {
  @Suppress("FunctionOnlyReturningConstant", "FunctionExpressionBody")
  @PostMapping("/prisons/{prisonId}/offenders/{offenderNo}/addHold")
  fun addHold(
    @PathVariable prisonId: String,
    @PathVariable offenderNo: String,
    @RequestBody request: AddHoldClientRequest,
  ): AddHoldResponse = prisonFinanceService.addHold(prisonId, offenderNo, request)

  @PostMapping("/prisons/{prisonId}/offenders/{offenderNo}/releaseHold/{holdNumber}")
  fun releaseHold(
    @PathVariable prisonId: String,
    @PathVariable offenderNo: String,
    @PathVariable holdNumber: Number,
  ): ResponseEntity<Void> = prisonFinanceService.releaseHold(prisonId, offenderNo, holdNumber)

  @PostMapping("/prisons/{prisonId}/offenders/{offenderNo}/releaseHoldCreateTransaction/{holdNumber}")
  fun releaseHoldAndCreateTransaction(
    @PathVariable prisonId: String,
    @PathVariable offenderNo: String,
    @PathVariable holdNumber: Number,
    @RequestBody request: ReleaseHoldCreateClientTransactionRequest,
  ): ReleaseHoldCreateTransactionResponse = prisonFinanceService.releaseHoldAndCreateTransaction(prisonId, offenderNo, holdNumber, request)
}
