package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CancelDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetDraftOrderResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.MedusaOrderService

@Tag(
  name = "Prisoner payment controller",
  description = "Prisoner finance and payment operations",
)
@RestController
@Validated
@PreAuthorize("hasRole('ROLE_PIN_PHONE_CREDIT_API')")
@RequestMapping(value = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(
  name = "Canteen Order",
  description = "Canteen medusa endpoints for order operations",
)
class MedusaOrderController(
  private val canteenOrderService: MedusaOrderService,
) {

  @Operation(summary = "Get the prisoners current draft order")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Medusa draft order successfully returned"),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  @GetMapping("/get-draft-order/{orderId}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getDraftOrder(
    @PathVariable orderId: String,
  ): GetDraftOrderResponse = canteenOrderService.getDraftOrder(orderId)

  @Operation(
    summary = "Create a draft order within medusa, containing items within cartId",
    responses = [
      ApiResponse(responseCode = "200", description = "Medusa draft order successfully created"),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  @PostMapping("/create-draft-order/{cartId}")
  fun createDraftOrder(
    @PathVariable cartId: String,
    @RequestBody createDraftOrderRequest: CreateDraftOrderRequest,
  ): CreateDraftOrderResponse = canteenOrderService.createDraftOrder(createDraftOrderRequest, cartId)

  @Operation(
    summary = "Cancel a draft order within medusa, containing items within cartId",
    responses = [
      ApiResponse(responseCode = "200", description = "Medusa draft order successfully canceled"),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  @PostMapping("/cancel-draft-order")
  fun cancelDraftOrder(
    @RequestBody cancelDraftOrderRequest: CancelDraftOrderRequest,
  ): ResponseEntity<Map<String, String>> = canteenOrderService.cancelOrder(cancelDraftOrderRequest)
}
