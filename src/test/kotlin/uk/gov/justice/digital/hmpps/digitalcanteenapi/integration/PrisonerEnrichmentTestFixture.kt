package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.CateringInstructions
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.DietAndAllergy
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.DietaryRequirementItem
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.DietaryRequirementList
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.DietaryRequirementValue
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.IepDetail
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.PrisonerHealthAndMedicationDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.PrisonerIncentivesDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.PrisonerSearchDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.Punishment
import java.time.LocalDate

object PrisonerEnrichmentTestFixture {
  const val PRISONER_NUMBER = "A1234BC"
  const val BOOKING_ID = "A1234BC"

  fun prisonerSearchDto(prisonerNumber: String = PRISONER_NUMBER) = PrisonerSearchDto(
    prisonerNumber = prisonerNumber,
    prisonId = "MDI",
    prisonName = "Moorland (HMP & YOI)",
    bookNumber = "12345",
    bookingId = BOOKING_ID,
    dateOfBirth = LocalDate.of(1990, 1, 15),
    youthOffender = false,
    gender = "Female",
  )

  fun healthAndMedicationDto(prisonerNumber: String = PRISONER_NUMBER) = PrisonerHealthAndMedicationDto(
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
  )

  fun prisonerIncentivesDto(prisonerNumber: String = PRISONER_NUMBER) = PrisonerIncentivesDto(
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
  )

  fun activePunishments() = listOf(
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
  )
}
