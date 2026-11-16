package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateMedusaUserRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetBasketRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetBasketResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveBasketRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaInternalClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.MedusaBasketService

@RestController
@PreAuthorize("hasRole('ROLE_PIN_PHONE_CREDIT_API')")
@RequestMapping("/api")
@Tag(
  name = "Canteen",
  description = "Canteen medusa endpoints for basket/cart operations",
)
class MedusaBasketController(
  private val medusaInternalClient: MedusaInternalClient,
  private val medusaBasketService: MedusaBasketService,
) {

  @Operation(summary = "Create prisoner user within medusa")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Medusa user created successfully"),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  @PostMapping("/create-medusa-user", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun createMedusaUser(
    @RequestBody createMedusaUserRequest: CreateMedusaUserRequest,
  ): ResponseEntity<Map<String, String>> = medusaInternalClient.createMedusaUser(createMedusaUserRequest)

  @Operation(summary = "Get the prisoners current basket")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Medusa user created successfully"),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  @PostMapping("/get-basket", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getBasket(
    @RequestBody getBasketRequest: GetBasketRequest,
  ): GetBasketResponse = medusaBasketService.getbasket(getBasketRequest)

  @Operation(summary = "Add line item to basket, if no basket exists, create basket then add line item")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Item added successfully"),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  @PostMapping("/add-line-item", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun addLineItem(
    @RequestBody addLineItemsRequest: AddLineItemsRequest,
  ): AddLineItemsResponse = medusaBasketService.addLineItem(addLineItemsRequest)

  @Operation(summary = "Remove line item from basket")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Item removed successfully"),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  @PostMapping("/remove-line-item", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun removeLineItem(
    @RequestBody removeLineItemsRequest: RemoveLineItemsRequest,
  ): RemoveLineItemsResponse = medusaBasketService.removeLineItem(removeLineItemsRequest)

  @Operation(summary = "Remove/cancel the medusa basket")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Medusa basket successfully removed"),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  @PostMapping("/remove-basket", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun removeBasket(
    @RequestBody removeBasket: RemoveBasketRequest,
  ): ResponseEntity<Map<String, String>> = medusaBasketService.removeBasket(removeBasket)
}
