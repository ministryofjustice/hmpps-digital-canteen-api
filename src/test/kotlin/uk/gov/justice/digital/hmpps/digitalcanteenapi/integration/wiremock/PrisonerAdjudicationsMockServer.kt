package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.Punishment
import java.time.LocalDate

const val OFFENDER_BOOKING_ID = "A1234BC"

class PrisonerAdjudicationsMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8081
  }

  private val mapper: JsonMapper = JsonMapper.builder().build()

  fun stubGetPrisonerAdjudication(offenderBookingId: String = OFFENDER_BOOKING_ID): StubMapping = stubFor(
    get("/reported-adjudications/punishments/$offenderBookingId/active")
      .willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(
            mapper.writeValueAsString(
              listOf(
                Punishment(
                  chargeNumber = "12345",
                  punishmentType = "PRIVILEGE",
                  privilegeType = "CANTEEN",
                  otherPrivilegeType = "none",
                  duration = 5,
                  measurement = "DAYS",
                  startDate = LocalDate.parse("2025-01-01"),
                  lastDay = LocalDate.parse("2025-01-31"),
                  amount = 0.1,
                  stoppagePercentage = 0,
                  activatedFrom = "today",
                ),
              ),
            ),
          )
          .withStatus(200),
      ),
  )
}
