package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.CartMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaCreateCartRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentResult
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentStatus
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.MedusaMockServer

class MedusaStoreClientTest {
  private lateinit var client: MedusaStoreClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    client = MedusaStoreClient(webClient)
  }

  @Test
  fun `getMedusaStoreTest- successfully returns store route request`() {
    server.stubGetMedusaStoreTest()

    val result = client.medusaStoreTest().block()

    assertThat(result).isNotNull
    with(result!!) {
      assertThat(status).isEqualTo("successful call to medusa store")
    }
  }

  @Test
  fun `createCart- successfully creates a cart`() {
    server.stubCreateCart()
    val cartRequest = MedusaCreateCartRequest(
      metadata = CartMetadata(
        prison_id = "MDI",
        offender_no = "A1234AA",
        first_name = "John",
        last_name = "Doe",
      ),
    )

    val result = client.createCart(cartRequest)

    assertThat(result).isNotNull
    assertThat(result.cart.id).isEqualTo("test-cart-id")
    assertThat(result.cart.currencyCode).isEqualTo("gbp")
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
