package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CancelDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CancelDraftOrderRequestData
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderRequestData
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetDraftOrderResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaOrderClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.CART_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.DRAFT_ORDER_ID

class MedusaOrderServiceTest {

  private val prisonerSearchClient: PrisonerSearchClient = mock()
  private val prisonFinanceClient: PrisonFinanceClient = mock()
  private val medusaOrderClient: MedusaOrderClient = mock()
  private val service = MedusaOrderService(prisonerSearchClient, prisonFinanceClient, medusaOrderClient)

  @Test
  fun `createDraftOrder - returns response from client`() {
    val request = CreateDraftOrderRequest(CreateDraftOrderRequestData(draftOrderId = null))
    val response = CreateDraftOrderResponse(draftOrderId = DRAFT_ORDER_ID)
    whenever(medusaOrderClient.createDraftOrder(request, CART_ID)).thenReturn(response)

    val result = service.createDraftOrder(request, CART_ID)

    assertThat(result).isEqualTo(response)
    verify(medusaOrderClient).createDraftOrder(request, CART_ID)
  }

  @Test
  fun `getDraftOrder - returns response from client`() {
    val response = GetDraftOrderResponse(hasDraftOrder = true)
    whenever(medusaOrderClient.getDraftOrder(DRAFT_ORDER_ID)).thenReturn(response)

    val result = service.getDraftOrder(DRAFT_ORDER_ID)

    assertThat(result).isEqualTo(response)
    verify(medusaOrderClient).getDraftOrder(DRAFT_ORDER_ID)
  }

  @Test
  fun `cancelOrder - returns response from client`() {
    val request = CancelDraftOrderRequest(CancelDraftOrderRequestData(draftOrderId = DRAFT_ORDER_ID))
    val response = ResponseEntity.ok(mapOf("status" to "success"))
    whenever(medusaOrderClient.cancelOrder(request)).thenReturn(response)

    val result = service.cancelOrder(request)

    assertThat(result).isEqualTo(response)
    verify(medusaOrderClient).cancelOrder(request)
  }
}
