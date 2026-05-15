package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class OpenFoodFactsApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val openFoodFactsApi = OpenFoodFactsMockServer(8095)

    @JvmField
    val openProductsFactsApi = OpenFoodFactsMockServer(8096)
  }

  override fun beforeAll(context: ExtensionContext) {
    openFoodFactsApi.start()
    openProductsFactsApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    openFoodFactsApi.resetRequests()
    openProductsFactsApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    openFoodFactsApi.stop()
    openProductsFactsApi.stop()
  }
}

class OpenFoodFactsMockServer(port: Int) : WireMockServer(port) {

  fun stubGetProduct(ean: String, responseBody: String) {
    stubFor(
      get(urlEqualTo("/api/v0/product/$ean.json"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
            .withStatus(200),
        ),
    )
  }

  fun stubGetProductNotFound(ean: String) {
    stubFor(
      get(urlEqualTo("/api/v0/product/$ean.json"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(404),
        ),
    )
  }
}
