package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.PinPhoneBuyCreditOrchestrationService
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.CompleteCartRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.CompleteCartResponse

@RestController
@PreAuthorize("permitAll()")
@RequestMapping("/api")
class APIController(private val pinPhoneBuyCreditOrchestrationService: PinPhoneBuyCreditOrchestrationService) {

  @PostMapping("/{cartId}/complete", produces = [MediaType.APPLICATION_JSON_VALUE])
  suspend fun completeCart(
    @PathVariable cartId: String,
    @RequestBody request: CompleteCartRequest
  ):CompleteCartResponse
  = pinPhoneBuyCreditOrchestrationService.processCheckout(request.offenderNo, request.amount, cartId)
}
