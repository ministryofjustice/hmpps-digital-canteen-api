package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetBasketRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetBasketResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveBasketRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaInternalClient

@Service
class MedusaBasketService(
  private val medusaInternalClient: MedusaInternalClient,
) {

  // todo: May not require service class, have included based on assumption more cart/basket logic will come to follow

  fun getbasket(getBasketRequest: GetBasketRequest): GetBasketResponse = medusaInternalClient.getBasket(getBasketRequest)

  fun addLineItem(addLineItemsRequest: AddLineItemsRequest): AddLineItemsResponse = medusaInternalClient.addLineItem(addLineItemsRequest)

  fun removeLineItem(removeLineItemsRequest: RemoveLineItemsRequest): RemoveLineItemsResponse = medusaInternalClient.removeLineItem(removeLineItemsRequest)

  fun removeBasket(removeBasketRequest: RemoveBasketRequest): ResponseEntity<Map<String, String>> = medusaInternalClient.removeBasket(removeBasketRequest)
}
