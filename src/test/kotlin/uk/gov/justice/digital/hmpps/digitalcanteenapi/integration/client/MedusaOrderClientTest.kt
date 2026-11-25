package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CancelDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CancelDraftOrderRequestData
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderRequestData
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaOrderClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.CART_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.DRAFT_ORDER_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.MedusaMockServer
import java.math.BigDecimal

class MedusaOrderClientTest {
  private lateinit var client: MedusaOrderClient

  @BeforeEach
  fun resetMocks() {
    server.resetAll()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    val mapper = JsonMapper.builder()
      .findAndAddModules()
      .build()
    val webClientErrorHandler = WebClientErrorHandler(mapper)
    client = MedusaOrderClient(webClient, webClientErrorHandler)
  }

  @Test
  fun `createDraftOrder - creates a new draft order when no draftOrderId`() {
    server.stubCreateDraftOrder()
    val request = CreateDraftOrderRequest(CreateDraftOrderRequestData(draftOrderId = null))

    val result = client.createDraftOrder(request, CART_ID)

    assertThat(result.draftOrderId).isEqualTo(DRAFT_ORDER_ID)
  }

  @Test
  fun `createDraftOrder - sends existing draftOrderId to add items to existing draft order`() {
    server.stubCreateDraftOrder()
    val request = CreateDraftOrderRequest(CreateDraftOrderRequestData(draftOrderId = DRAFT_ORDER_ID))

    val result = client.createDraftOrder(request, CART_ID)

    assertThat(result.draftOrderId).isEqualTo(DRAFT_ORDER_ID)
  }

  @Test
  fun `getDraftOrder - successfully retrieves draft order with items`() {
    server.stubGetDraftOrder()

    val result = client.getDraftOrder(DRAFT_ORDER_ID)

    assertThat(result.hasDraftOrder).isTrue()
    assertThat(result.items).hasSize(1)
    assertThat(result.items?.get(0)?.title).isEqualTo("Banana")
    assertThat(result.items?.get(0)?.quantity).isEqualTo(3)
    assertThat(result.items?.get(0)?.unitPrice).isEqualByComparingTo(BigDecimal.valueOf(85))
    assertThat(result.items?.get(0)?.lineTotal).isEqualByComparingTo(BigDecimal.valueOf(255))
    assertThat(result.itemCount).isEqualTo(3)
    assertThat(result.subtotal).isEqualByComparingTo(BigDecimal.valueOf(255))
    assertThat(result.total).isEqualByComparingTo(BigDecimal.valueOf(255))
  }

  @Test
  fun `cancelOrder - successfully cancels draft order`() {
    server.stubCancelOrder()
    val request = CancelDraftOrderRequest(CancelDraftOrderRequestData(draftOrderId = DRAFT_ORDER_ID))

    val result = client.cancelOrder(request)

    assertThat(result.statusCode.value()).isEqualTo(200)
    assertThat(result.body).containsEntry("status", "success")
  }

  companion object {
    @JvmField
    internal val server = MedusaMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      server.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      server.stop()
    }
  }
}
