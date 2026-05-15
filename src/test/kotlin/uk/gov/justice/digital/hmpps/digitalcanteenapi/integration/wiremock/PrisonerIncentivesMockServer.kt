package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PrisonerEnrichmentTestFixture

class PrisonerIncentivesMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8083
  }

  private val mapper: JsonMapper = JsonMapper.builder().build()

  fun stubGetPrisoner(prisonerNumber: String = PRISONER_NUMBER): StubMapping = stubFor(
    get("/incentive-reviews/prisoner/$prisonerNumber")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            mapper.writeValueAsString(
              PrisonerEnrichmentTestFixture.prisonerIncentivesDto(),
            ),
          )
          .withStatus(200),
      ),
  )
}
