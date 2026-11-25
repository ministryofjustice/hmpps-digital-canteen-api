package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.CART_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.DRAFT_ORDER_ID

class MedusaMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8087

    // Keep in step with MedusaOrderClient
    const val CREATE_DRAFT_ORDER_PATH = "/internal/canteen/orders/%s/create-draft-order"
    const val GET_DRAFT_ORDER_PATH = "/internal/canteen/orders/%s/get-draft-order"
  }

  private val mapper: JsonMapper = JsonMapper.builder().build()

  fun stubGetAdminToken(): StubMapping = stubFor(
    post("/auth/user/emailpass")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(mapper.writeValueAsString(mapOf("token" to "test-token")))
          .withStatus(200),
      ),
  )

  fun stubCreateMedusaUser(): StubMapping = stubFor(
    post("/internal/canteen/create-user")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(200),
      ),
  )

  fun stubAddLineItem(): StubMapping = stubFor(
    post("/internal/canteen/carts/add-line-item")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(mapper.writeValueAsString(mapOf("cartId" to CART_ID)))
          .withStatus(200),
      ),
  )

  fun stubGetBasket(
    items: List<Map<String, Any>> = listOf(
      mapOf(
        "lineItemId" to "cali_01M6GHP27VFW7096R5AQ0G5EHT",
        "variantId" to "variant_01M6E4F0KWV67V7JV3TN7DMAM7",
        "title" to "Cheeeeeeseeee",
        "quantity" to 3,
        "unitPrice" to 11,
        "lineTotal" to 33,
        "allItemsTotal" to 33,
      ),
    ),
    itemCount: Int = 3,
  ): StubMapping = stubFor(
    post("/internal/canteen/carts/get-basket")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            mapper.writeValueAsString(
              mapOf(
                "cartId" to CART_ID,
                "items" to items,
                "itemCount" to itemCount,
              ),
            ),
          )
          .withStatus(200),
      ),
  )

  fun stubGetBasketEmpty(): StubMapping = stubFor(
    post("/internal/canteen/carts/get-basket")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            mapper.writeValueAsString(
              mapOf(
                "cartId" to null,
                "items" to emptyList<Any>(),
                "itemCount" to 0,
              ),
            ),
          )
          .withStatus(200),
      ),
  )

  fun stubRemoveLineItem(): StubMapping = stubFor(
    post("/internal/canteen/carts/remove-line-item")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(mapper.writeValueAsString(mapOf("cartId" to CART_ID)))
          .withStatus(200),
      ),
  )

  fun stubRemoveBasket(): StubMapping = stubFor(
    post("/internal/canteen/carts/remove-basket")
      .willReturn(
        aResponse()
          .withStatus(200),
      ),
  )

  fun stubCreateDraftOrder(
    cartId: String = CART_ID,
    draftOrderId: String = DRAFT_ORDER_ID,
    status: Int = 200,
  ): StubMapping = stubFor(
    post(urlPathEqualTo(CREATE_DRAFT_ORDER_PATH.format(cartId)))
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            mapper.writeValueAsString(mapOf("draftOrderId" to draftOrderId)),
          )
          .withStatus(status),
      ),
  )

  fun stubGetDraftOrder(
    draftOrderId: String = DRAFT_ORDER_ID,
    items: List<Map<String, Any>> = listOf(
      mapOf(
        "title" to "Banana",
        "quantity" to 3,
        "unitPrice" to 85,
        "lineTotal" to 255,
      ),
    ),
    itemCount: Int = 3,
    subtotal: Int = 255,
    total: Int = 255,
  ): StubMapping = stubFor(
    get(urlPathEqualTo(GET_DRAFT_ORDER_PATH.format(draftOrderId)))
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            mapper.writeValueAsString(
              mapOf(
                "hasDraftOrder" to true,
                "items" to items,
                "itemCount" to itemCount,
                "subtotal" to subtotal,
                "total" to total,
              ),
            ),
          )
          .withStatus(200),
      ),
  )

  fun stubCancelOrder(): StubMapping = stubFor(
    post("/internal/canteen/orders/cancel-draft-order")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(200),
      ),
  )

  fun stubGetProducts(
    responseBody: String,
    limit: Int? = null,
    offset: Int? = null,
  ): StubMapping {
    val mapping = get(urlPathEqualTo("/admin/products"))
    limit?.let {
      mapping.withQueryParam("limit", equalTo(it.toString()))
    }
    offset?.let {
      mapping.withQueryParam("offset", equalTo(it.toString()))
    }
    return stubFor(
      mapping.willReturn(
        aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "application/json")
          .withBody(responseBody),
      ),
    )
  }

  fun stubBatchProducts(responseBody: String): StubMapping = stubFor(
    post("/admin/products/batch")
      .willReturn(
        aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "application/json")
          .withBody(responseBody),
      ),
  )

  fun stubGetProductsWithStatus(
    status: Int,
    responseBody: String,
  ): StubMapping = stubFor(
    get(urlPathEqualTo("/admin/products"))
      .withQueryParam("limit", matching(".*"))
      .withQueryParam("offset", matching(".*"))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader("Content-Type", "application/json")
          .withBody(responseBody),
      ),
  )
}

class MedusaApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val medusaApi = MedusaMockServer()
  }

  override fun beforeAll(context: ExtensionContext): Unit = medusaApi.start()
  override fun beforeEach(context: ExtensionContext) {
    medusaApi.resetAll()
    medusaApi.stubGetAdminToken()
    medusaApi.stubAddLineItem()
    medusaApi.stubCreateMedusaUser()
    medusaApi.stubGetBasket()
    medusaApi.stubGetBasketEmpty()
    medusaApi.stubRemoveLineItem()
    medusaApi.stubRemoveBasket()
    medusaApi.stubCreateDraftOrder()
    medusaApi.stubGetDraftOrder()
    medusaApi.stubCancelOrder()
  }

  override fun afterAll(context: ExtensionContext): Unit = medusaApi.stop()
}
