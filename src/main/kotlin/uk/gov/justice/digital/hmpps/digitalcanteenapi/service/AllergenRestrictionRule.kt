package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import com.deliveredtechnologies.rulebook.RuleState
import com.deliveredtechnologies.rulebook.annotation.*

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.productenrichment.dto.ProductDetailsResponse

@Rule(order = 2, name = "AllergenRestrictionRule")
class AllergenRestrictionRule {

  @Given("prisoner")
  private lateinit var prisoner: PrisonerEnrichmentService.EnrichedPrisonerDto

  @Given("product")
  private lateinit var product: ProductDetailsResponse

  @Result
  private var result: RestrictionResult = RestrictionResult(
    restricted = false,
    restrictionType = null,
    reason = null
  )

  private val matchedAllergens = mutableListOf<String>()

  @When
  fun isApplicable(): Boolean {
    val allergenStr = product.product?.allergens ?: return false
    if (allergenStr.isBlank()) return false

    val productAllergens = allergenStr.split(",")
      .map { it.trim().lowercase() }
      .filter { it.isNotEmpty() }

    val allergenMapping = mapOf(
      "celery" to "FOOD_ALLERGY_CELERY",
      "gluten" to "FOOD_ALLERGY_GLUTEN",
      "crustaceans" to "FOOD_ALLERGY_CRUSTACEANS",
      "egg" to "FOOD_ALLERGY_EGG",
      "eggs" to "FOOD_ALLERGY_EGG",
      "fish" to "FOOD_ALLERGY_FISH",
      "lupin" to "FOOD_ALLERGY_LUPIN",
      "milk" to "FOOD_ALLERGY_MILK",
      "dairy" to "FOOD_ALLERGY_MILK",
      "molluscs" to "FOOD_ALLERGY_MOLLUSCS",
      "mollusks" to "FOOD_ALLERGY_MOLLUSCS",
      "mustard" to "FOOD_ALLERGY_MUSTARD",
      "peanut" to "FOOD_ALLERGY_PEANUTS",
      "peanuts" to "FOOD_ALLERGY_PEANUTS",
      "sesame" to "FOOD_ALLERGY_SESAME",
      "soya" to "FOOD_ALLERGY_SOYA",
      "soybeans" to "FOOD_ALLERGY_SOYA",
      "soy" to "FOOD_ALLERGY_SOYA",
      "sulphur" to "FOOD_ALLERGY_SULPHUR_DIOXIDE",
      "sulfur" to "FOOD_ALLERGY_SULPHUR_DIOXIDE",
      "nuts" to "FOOD_ALLERGY_TREE_NUTS"
    )

    val productAllergenCodes = productAllergens.mapNotNull { allergenMapping[it] }
    val prisonerAllergens = prisoner.foodAllergies ?: emptyList()

    productAllergenCodes.forEach { code ->
      if (prisonerAllergens.contains(code)) {
        matchedAllergens.add(code)
      }
    }
    return matchedAllergens.isNotEmpty()
  }

  @Then
  fun execute(): RuleState {
    result = RestrictionResult(
      restricted = true,
      restrictionType = "ALLERGEN",
      reason = matchedAllergens.joinToString { it }
    )
    return RuleState.NEXT
  }
}