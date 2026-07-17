package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.CompleteCartRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.CompleteCartResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.PinPhoneBuyCreditOrchestrationService

@RestController
@PreAuthorize("permitAll()")
@RequestMapping("/api")
@Tag(name = "Purchase",
     description = "Purchase API"
)
class PurchaseController(private val pinPhoneBuyCreditOrchestrationService: PinPhoneBuyCreditOrchestrationService) {

  @Suppress("MaxLineLength")
  @Operation(summary = "Completes the cart and processes the checkout for PIN Phone credit purchase")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Purchase completed successfully"),
      ApiResponse(responseCode = "404", description = "Prisoner not found"),
      ApiResponse(responseCode = "400", description = "Bad request or upstream error"),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  @PostMapping("/{cartId}/complete", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun completeCart(@PathVariable cartId: String, @RequestBody request: CompleteCartRequest): CompleteCartResponse = pinPhoneBuyCreditOrchestrationService.processCheckout(request.offenderNo, request.amount, cartId)
}
