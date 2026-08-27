package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CartMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateCartRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentResult
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentStatus
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.MedusaMockServer

class MedusaStoreClientTest {
  private lateinit var client: MedusaStoreClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    val mapper = JsonMapper.builder()
      .findAndAddModules()
      .build()
    val webClientErrorHandler = WebClientErrorHandler(mapper)
    client = MedusaStoreClient(webClient, webClientErrorHandler)
  }

  @Test
  fun `createCart- successfully creates a cart`() {
    server.stubCreateCart()
    val cartRequest = CreateCartRequest(
      CartMetadata(
        prisonId = "MDI",
        offenderNo = "A1234AA",
        firstName = "John",
        secondName = "Doe",
      ),
    )

    val result = client.createCart(cartRequest)

    assertThat(result).isNotNull
    assertThat(result.cart?.id).isEqualTo("test-cart-id")
  }

  @Test
  fun `addPinPhoneItemsToCart - successfully adds line item`() {
    val cartId = "test-cart-id"
    server.stubAddLineItem(cartId)
    val addItemsRequest = AddItemsRequest(amount = 500)

    val result = client.addPinPhoneItemsToCart(addItemsRequest, cartId)

    assertThat(result).isNotNull
    assertThat(result.cart?.id).isEqualTo(cartId)
  }

  @Test
  fun `completeCart- successfully completes a cart`() {
    val cartId = "test-cart-id"
    server.stubCompleteCart(cartId)
    val paymentResult = PaymentResult(
      offender_no = "A1234AA",
      status = PaymentStatus.AUTHORIZED,
      transactionReference = "ref-123",
      holdNumber = 12345,
      errorCode = null,
      errorMessage = null,
    )

    val result = client.completeCart(cartId, paymentResult)

    assertThat(result).isNotNull
    assertThat(result.order?.id).isEqualTo("test-order-id")
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
