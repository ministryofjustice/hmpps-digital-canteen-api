package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.CateringInstructions
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.DietAndAllergy
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.DietaryRequirementItem
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.DietaryRequirementList
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.DietaryRequirementValue
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model.HealthAndMedicationDto

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
              HealthAndMedicationDto(
                dietAndAllergy = DietAndAllergy(
                  foodAllergies = DietaryRequirementList(
                    value = listOf(
                      DietaryRequirementItem(
                        value = DietaryRequirementValue(
                          id = "FOOD_ALLERGY_NUTS",
                          code = "NUTS",
                          description = "Nut allergy",
                        ),
                        comment = "Severe reaction to peanuts",
                      ),
                    ),
                    lastModifiedAt = "2025-01-15T10:30:00",
                    lastModifiedBy = "STAFF_USER",
                    lastModifiedPrisonId = "MDI",
                  ),
                  medicalDietaryRequirements = DietaryRequirementList(
                    value = listOf(
                      DietaryRequirementItem(
                        value = DietaryRequirementValue(
                          id = "DIET_GLUTEN_FREE",
                          code = "GLUTEN_FREE",
                          description = "Gluten free diet",
                        ),
                        comment = "Celiac disease",
                      ),
                    ),
                    lastModifiedAt = "2025-01-10T14:20:00",
                    lastModifiedBy = "MEDICAL_STAFF",
                    lastModifiedPrisonId = "MDI",
                  ),
                  personalisedDietaryRequirements = DietaryRequirementList(
                    value = listOf(
                      DietaryRequirementItem(
                        value = DietaryRequirementValue(
                          id = "DIET_HALAL",
                          code = "HALAL",
                          description = "Halal diet",
                        ),
                        comment = "Religious requirement",
                      ),
                    ),
                    lastModifiedAt = "2025-01-05T09:15:00",
                    lastModifiedBy = "Test",
                    lastModifiedPrisonId = "MDI",
                  ),
                  cateringInstructions = CateringInstructions(
                    value = "Cake",
                    lastModifiedAt = "2025-01-05T09:15:00",
                    lastModifiedBy = "Test",
                    lastModifiedPrisonId = "MDI",
                  ),
                  topLevelLocation = "Somewhere",
                  lastAdmissionDate = "2024-12-01",
                ),
              ),
            ),
          )
          .withStatus(200),
      ),
  )
}
