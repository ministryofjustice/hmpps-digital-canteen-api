package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.PrisonerSearchDto
import java.time.LocalDate
import kotlin.String

class PrisonerSearchMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8081
  }

  private val mapper: JsonMapper = JsonMapper.builder().build()

  fun stubGetPrisoner(prisonerNumber: String = PRISONER_NUMBER): StubMapping = stubFor(
    get("/prisoner/$prisonerNumber")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            mapper.writeValueAsString(
              PrisonerSearchDto(
                prisonerNumber = prisonerNumber,
                prisonId = "MDI",
                prisonName = "Moorland (HMP & YOI)",
                bookNumber = "12345",
                bookingId = "12345",
                dateOfBirth = LocalDate.of(1990, 1, 15),
                youthOffender = false,
                gender = "Female",
              ),
            ),
          )
          .withStatus(200),
      )
  )
}