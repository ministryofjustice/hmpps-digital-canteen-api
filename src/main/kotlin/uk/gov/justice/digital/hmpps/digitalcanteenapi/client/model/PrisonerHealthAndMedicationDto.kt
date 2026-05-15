package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model

data class PrisonerHealthAndMedicationDto(
  val dietAndAllergy: DietAndAllergy,
)

data class DietAndAllergy(
  val foodAllergies: DietaryRequirementList?,
  val medicalDietaryRequirements: DietaryRequirementList?,
  val personalisedDietaryRequirements: DietaryRequirementList?,
  val cateringInstructions: CateringInstructions?,
  val topLevelLocation: String?,
  val lastAdmissionDate: String?,
)

data class DietaryRequirementList(
  val value: List<DietaryRequirementItem>,
  val lastModifiedAt: String,
  val lastModifiedBy: String,
  val lastModifiedPrisonId: String,
)

data class DietaryRequirementItem(
  val value: DietaryRequirementValue,
  val comment: String?,
)

data class DietaryRequirementValue(
  val id: String,
  val code: String,
  val description: String,
)

data class CateringInstructions(
  val value: String?,
  val lastModifiedAt: String,
  val lastModifiedBy: String,
  val lastModifiedPrisonId: String,
)
