package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.CART_ID

class MedusaMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8087
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
  }

  override fun afterAll(context: ExtensionContext): Unit = medusaApi.stop()
}
