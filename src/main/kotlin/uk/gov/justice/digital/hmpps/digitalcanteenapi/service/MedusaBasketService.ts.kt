package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaInternalClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.controller.MedusaController

@Service
class MedusaBasketService(
  private val medusaInternalClient: MedusaInternalClient
) {


  fun getbasket(getBasketRequest: MedusaController.GetBasketRequest): MedusaController.GetBasketResponse {

    println(getBasketRequest)

//    val basket = medusaInternalClient.getBasket(getBasketRequest)
//    println(basket)

    val response = MedusaController.GetBasketResponse(
      cart = MedusaController.Cart(
          cartId = "1234",
          originalItemTotal = 10,
          itemTotal = 10,
          itemSubtotal = 10)
    )

    return response

  }

  fun addLineItem(addLineItemsRequest: AddLineItemsRequest): AddLineItemsResponse {
    println(addLineItemsRequest)

    return medusaInternalClient.addLineItem(addLineItemsRequest)

  }

  fun removeLineItem(removeLineItemsRequest: RemoveLineItemsRequest): RemoveLineItemsResponse{
    println(removeLineItemsRequest)

    return medusaInternalClient.removeLineItem(removeLineItemsRequest)

  }
}