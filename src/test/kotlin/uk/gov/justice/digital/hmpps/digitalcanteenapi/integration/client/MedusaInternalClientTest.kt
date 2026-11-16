package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.AddLineItemsRequestMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateMedusaUserMetaData
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateMedusaUserRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetBasketRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetBasketRequestMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveBasketMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveBasketRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.RemoveLineItemsRequestMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaInternalClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.CART_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISON_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.MedusaMockServer
import java.math.BigDecimal

class MedusaInternalClientTest {
  private lateinit var client: MedusaInternalClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    val mapper = JsonMapper.builder()
      .findAndAddModules()
      .build()
    val webClientErrorHandler = WebClientErrorHandler(mapper)
    client = MedusaInternalClient(webClient, webClientErrorHandler)
  }

  @Test
  fun `createMedusaUser - successfully creates a user`() {
    server.stubCreateMedusaUser()
    val request = CreateMedusaUserRequest(
      CreateMedusaUserMetaData(
        prisonId = PRISON_ID,
        offenderNo = PRISONER_NUMBER,
      ),
    )

    val result = client.createMedusaUser(request)

    assertThat(result.statusCode.value()).isEqualTo(200)
    assertThat(result.body).containsEntry("status", "success")
  }

  @Test
  fun `createMedusaUser - throws UpstreamException when service is down`() {
    server.stop()
    try {
      val request = CreateMedusaUserRequest(
        CreateMedusaUserMetaData(
          prisonId = PRISON_ID,
          offenderNo = PRISONER_NUMBER,
        ),
      )
      val exception = assertThrows<UpstreamException> {
        client.createMedusaUser(request)
      }
      assertThat(exception.message).contains("Medusa service is currently unavailable")
    } finally {
      server.start()
    }
  }

  @Test
  fun `getBasket - successfully retrieves basket`() {
    server.stubGetBasket()
    val request = GetBasketRequest(
      GetBasketRequestMetadata(
        offenderNo = PRISONER_NUMBER,
        cartId = CART_ID,
      ),
    )

    val result = client.getBasket(request)

    assertThat(result.cartId).isEqualTo(CART_ID)
    assertThat(result.items).hasSize(1)
    assertThat(result.items?.get(0)?.title).isEqualTo("Cheeeeeeseeee")
    assertThat(result.itemCount).isEqualTo(BigDecimal.valueOf(3))
  }

  @Test
  fun `getBasket - returns empty basket`() {
    server.stubGetBasketEmpty()
    val request = GetBasketRequest(
      GetBasketRequestMetadata(
        offenderNo = PRISONER_NUMBER,
        cartId = null,
      ),
    )

    val result = client.getBasket(request)

    assertThat(result.cartId).isNull()
    assertThat(result.items).isEmpty()
    assertThat(result.itemCount).isEqualTo(BigDecimal.valueOf(0))
  }

  @Test
  fun `addLineItem - successfully adds item to cart`() {
    server.stubAddLineItem()
    val request = AddLineItemsRequest(
      AddLineItemsRequestMetadata(
        prisonId = PRISON_ID,
        offenderNo = PRISONER_NUMBER,
        cartId = CART_ID,
        productVariantId = "var_1234",
      ),
    )

    val result = client.addLineItem(request)
    assertThat(result.cartId).isEqualTo(CART_ID)
  }

  @Test
  fun `removeLineItem - successfully removes item from cart`() {
    server.stubRemoveLineItem()
    val request = RemoveLineItemsRequest(
      RemoveLineItemsRequestMetadata(
        cartId = CART_ID,
        lineItemId = "li_123",
        quantity = "0",
      ),
    )

    val result = client.removeLineItem(request)
    assertThat(result.cartId).isEqualTo(CART_ID)
  }

  @Test
  fun `removeBasket - successfully removes basket`() {
    server.stubRemoveBasket()
    val request = RemoveBasketRequest(
      RemoveBasketMetadata(
        cartId = CART_ID,
      ),
    )

    val result = client.removeBasket(request)
    assertThat(result.statusCode.value()).isEqualTo(200)
    assertThat(result.body).containsEntry("status", "success")
  }

  @Test
  fun `removeBasket - throws UpstreamException when service is down`() {
    server.stop()
    try {
      val request = RemoveBasketRequest(
        RemoveBasketMetadata(
          cartId = CART_ID,
        ),
      )
      val exception = assertThrows<UpstreamException> {
        client.removeBasket(request)
      }
      assertThat(exception.message).contains("Medusa service is currently unavailable")
    } finally {
      server.start()
    }
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
