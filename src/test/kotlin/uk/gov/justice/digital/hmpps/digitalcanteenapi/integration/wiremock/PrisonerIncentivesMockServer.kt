package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.IepDetail
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.PrisonerIncentivesDto

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
              PrisonerIncentivesDto(
                id = 12345L,
                iepCode = "STD",
                iepLevel = "Standard",
                prisonerNumber = prisonerNumber,
                bookingId = 123456L,
                iepDate = "2025-01-15",
                iepTime = "14:30:00",
                iepDetails = listOf(
                  IepDetail(
                    id = 12345L,
                    iepLevel = "Standard",
                    iepCode = "STD",
                    comments = "something",
                    prisonerNumber = prisonerNumber,
                    bookingId = 123456L,
                    iepDate = "2025-01-15",
                    iepTime = "14:30:00",
                    agencyId = "test",
                    userId = "STAFF_USER",
                    reviewType = "REVIEW",
                    auditModuleName = "test",
                    isRealReview = true,
                  ),
                ),
                nextReviewDate = "2025-07-15",
                daysSinceReview = 30,
              ),
            ),
          )
          .withStatus(200),
      ),
  )
}
