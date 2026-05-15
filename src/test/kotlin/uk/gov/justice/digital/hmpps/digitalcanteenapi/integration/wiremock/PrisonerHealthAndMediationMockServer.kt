package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PrisonerEnrichmentTestFixture

class PrisonerHealthAndMediationMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8082
  }

  private val mapper: JsonMapper = JsonMapper.builder().build()

  fun stubGetPrisoner(prisonerNumber: String = PRISONER_NUMBER): StubMapping = stubFor(
    get("/prisoners/$prisonerNumber")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            mapper.writeValueAsString(
              PrisonerEnrichmentTestFixture.healthAndMedicationDto(),
            ),
          )
          .withStatus(200),
      ),
  )
}
