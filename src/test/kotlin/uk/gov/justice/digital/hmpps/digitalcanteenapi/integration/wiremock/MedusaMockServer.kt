package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaDto

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

  fun stubGetMedusaAdminTest(): StubMapping {
    stubGetAdminToken()
    return stubFor(
      get("/admin/test-request-from-api")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              mapper.writeValueAsString(
                MedusaDto("successful call to medusa admin"),
              ),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubGetMedusaStoreTest(): StubMapping = stubFor(
    get("/store/test-request-from-api")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(mapper.writeValueAsString(MedusaDto("successful call to medusa store")))
          .withStatus(200),
      ),
  )
}
