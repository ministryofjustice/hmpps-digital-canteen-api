package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.IepDetail
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.PrisonerIncentivesDto
import kotlin.String

class PrisonerIncentivesMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8081
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
                iepLevel = "standard",
                prisonerNumber = prisonerNumber,
                bookingId = 12345L,
                iepDate = "aDate",
                iepTime = "atime",
                iepDetails = listOf(IepDetail(
                  id = 12345L,
                  iepLevel = "test",
                  iepCode = "test",
                  comments = "test",
                  prisonerNumber = prisonerNumber,
                  bookingId = 12345L,
                  iepDate = "test",
                  iepTime = "test",
                  agencyId = "test",
                  userId = "test",
                  reviewType = "test",
                  auditModuleName = "test",
                  isRealReview = false,
                )),
                nextReviewDate = "sometime",
                daysSinceReview = 2,
              ),
            ),
          )
          .withStatus(200),
      )
  )
}