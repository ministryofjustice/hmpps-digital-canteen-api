package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CartResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient

@RestController
@PreAuthorize("permitAll()")
@RequestMapping("/api")
@Tag(
  name = "Pin Phone",
  description = "Pin Phone medusa endpoints",
)
class MedusaController(private val medusaStoreClient: MedusaStoreClient) {

  @Operation(summary = "Add pin phone product to Medusa cart")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Line item added successfully"),
      ApiResponse(responseCode = "400", description = "Invalid request"),
      ApiResponse(responseCode = "404", description = "Cart or variant not found"),
      ApiResponse(responseCode = "500", description = "Internal server error"),
    ],
  )
  @PostMapping("/add-line-item", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun addLineItem(
    @Valid @RequestBody addItemsToCartRequest: AddItemsToCartRequest,
  ): CartResponse = medusaStoreClient.addPinPhoneItemsToCart(addItemsToCartRequest)
}

data class AddItemsToCartRequest(
  @field:NotNull(message = "amount is required")
  val amount: Long,

  @field:NotBlank(message = "cartId is required")
  val cartId: String,
)

