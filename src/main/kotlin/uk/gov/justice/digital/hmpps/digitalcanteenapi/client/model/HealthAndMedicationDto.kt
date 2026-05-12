package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.ZonedDateTime


@Schema(description = "Prisoner health and medication data")
data class HealthAndMedicationDto(
  @Schema(description = "Diet and allergy")
  val dietAndAllergy: DietAndAllergyResponse? = null,
)

@Schema(description = "Diet and allergy data")
data class DietAndAllergyResponse(
  @Schema(description = "Food allergies")
  val foodAllergies: ValueWithMetadata<List<ReferenceDataSelection>>? = null,

  @Schema(description = "Medical dietary requirements")
  val medicalDietaryRequirements: ValueWithMetadata<List<ReferenceDataSelection>>? = null,

  @Schema(description = "Personalised dietary requirements")
  val personalisedDietaryRequirements: ValueWithMetadata<List<ReferenceDataSelection>>? = null,

  @Schema(description = "Catering instructions")
  val cateringInstructions: ValueWithMetadata<String?>? = null,

  @Schema(description = "The top level of the prisoner's location hierarchy e.g. Wing")
  val topLevelLocation: String? = null,

  @Schema(description = "The latest arrival date of the prisoner into prison")
  val lastAdmissionDate: LocalDate? = null,
)

data class ValueWithMetadata<T>(
  @Schema(description = "Value")
  val value: T?,

  @Schema(description = "Timestamp this field was last modified")
  val lastModifiedAt: ZonedDateTime,

  @Schema(description = "Username of the user that last modified this field", example = "USER1")
  val lastModifiedBy: String,

  @Schema(description = "The id code of the active prison at the time of the update", example = "STI")
  val lastModifiedPrisonId: String,
)

@Schema(description = "Reference data selection with comment")
data class ReferenceDataSelection(
  @Schema(description = "Selected reference data details", required = true)
  val value: ReferenceDataValue,

  @Schema(description = "User supplied comment about this selection", example = "Some other text", required = false)
  val comment: String? = null,
)

@Schema(description = "Reference Data Value - a reference data code selected as the value for a field")
@JsonInclude(NON_NULL)
data class ReferenceDataValue(
  @Schema(description = "Id", example = "FOOD_ALLERGY_MILK")
  val id: String,

  @Schema(description = "Code", example = "MILK")
  val code: String,

  @Schema(description = "Description of the reference data code", example = "Milk")
  val description: String,
)

