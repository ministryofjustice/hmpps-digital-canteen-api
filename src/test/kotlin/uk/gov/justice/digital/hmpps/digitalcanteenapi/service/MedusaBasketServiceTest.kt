package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsRequestData
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetBasketRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetBasketRequestData
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetBasketResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveBasketData
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveBasketRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsRequestData
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaBasketClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.CART_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISON_ID

class MedusaBasketServiceTest {

  private val client: MedusaBasketClient = mock()
  private val service = MedusaBasketService(client)

  @Test
  fun `getBasket - returns basket from client`() {
    val request = GetBasketRequest(GetBasketRequestData(offenderNo = PRISONER_NUMBER, cartId = CART_ID))
    val response = GetBasketResponse(cartId = CART_ID)
    whenever(client.getBasket(request)).thenReturn(response)

    val result = service.getBasket(request)

    assertThat(result).isEqualTo(response)
    verify(client).getBasket(request)
  }

  @Test
  fun `addLineItem - returns response from client`() {
    val request = AddLineItemsRequest(
      AddLineItemsRequestData(
        prisonId = PRISON_ID,
        offenderNo = PRISONER_NUMBER,
        cartId = CART_ID,
        productVariantId = "var_1234",
      ),
    )
    val response = AddLineItemsResponse(cartId = CART_ID)
    whenever(client.addLineItem(request)).thenReturn(response)

    val result = service.addLineItem(request)

    assertThat(result).isEqualTo(response)
    verify(client).addLineItem(request)
  }

  @Test
  fun `removeLineItem - returns response from client`() {
    val request = RemoveLineItemsRequest(
      RemoveLineItemsRequestData(cartId = CART_ID, lineItemId = "li_123", quantity = "0"),
    )
    val response = RemoveLineItemsResponse(cartId = CART_ID)
    whenever(client.removeLineItem(request)).thenReturn(response)

    val result = service.removeLineItem(request)

    assertThat(result).isEqualTo(response)
    verify(client).removeLineItem(request)
  }

  @Test
  fun `removeBasket - returns response from client`() {
    val request = RemoveBasketRequest(RemoveBasketData(cartId = CART_ID))
    val response = ResponseEntity.ok(mapOf("status" to "success"))
    whenever(client.removeBasket(request)).thenReturn(response)

    val result = service.removeBasket(request)

    assertThat(result).isEqualTo(response)
    verify(client).removeBasket(request)
  }
}
