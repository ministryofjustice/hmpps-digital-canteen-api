package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.OpenFoodFactsApiExtension.Companion.openFoodFactsApi
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.OpenFoodFactsApiExtension.Companion.openProductsFactsApi

class ProductEnrichmentInfoIntegrationTest : IntegrationTestBase() {

  @Test
  fun `can get product from open food facts with filtered tags`() {
    val ean = "3017620422003"
    openFoodFactsApi.stubGetProduct(ean, nutellaResponse(ean))

    webTestClient.get()
      .uri("/api/product/$ean")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.code").isEqualTo(ean)
      .jsonPath("$.product.product_name").isEqualTo("Nutella")
      .jsonPath("$.product.allergens").isEqualTo("milk,nuts,soybeans")
      .jsonPath("$.product.ingredients_analysis_tags.length()").isEqualTo(3)
      .jsonPath("$.product.ingredients_analysis_tags[0]").isEqualTo("palm-oil")
      .jsonPath("$.product.ingredients_analysis_tags[1]").isEqualTo("non-vegan")
      .jsonPath("$.product.ingredients_analysis_tags[2]").isEqualTo("vegetarian")
      .jsonPath("$.product.packaging_tags.length()").isEqualTo(1)
      .jsonPath("$.product.packaging_tags[0]").isEqualTo("plastic")
      .jsonPath("$.product.categories_tags.length()").isEqualTo(2)
      .jsonPath("$.product.categories_tags[0]").isEqualTo("breakfast-cereals")
      .jsonPath("$.product.categories_tags[1]").isEqualTo("chocolate-spreads")
      .jsonPath("$.product.nutriments.energy-kcal_100g").isEqualTo(539)
      .jsonPath("$.product.nutriments.fat_100g").isEqualTo(30.9)
      .jsonPath("$.product.nutriments.salt_100g").isEqualTo(0.107)
      .jsonPath("$.status").isEqualTo(1)
  }

  private fun nutellaResponse(ean: String) = """
      {
        "code": "$ean",
        "product": {
          "product_name": "Nutella",
          "brands": "Ferrero",
          "quantity": "400g",
          "image_url": "https://images.openfoodfacts.org/images/products/301/762/042/2003/front_fr.465.400.jpg",
          "allergens": "en:milk,en:nuts,en:soybeans,fr:lait",
          "ingredients_analysis_tags": [
            "en:palm-oil",
            "en:non-vegan",
            "en:vegetarian",
            "fr:vegetarien"
          ],
          "packaging_tags": [
            "en:plastic",
            "fr:pot-en-verre"
          ],
          "categories_tags": [
            "en:breakfast-cereals",
            "en:chocolate-spreads",
            "fr:pates-a-tartiner"
          ],
          "nutriments": {
            "energy-kcal_100g": 539,
            "fat_100g": 30.9,
            "saturated-fat_100g": 10.6,
            "carbohydrates_100g": 57.5,
            "sugars_100g": 56.3,
            "proteins_100g": 6.3,
            "salt_100g": 0.107
          }
        },
        "status": 1,
        "status_verbose": "product found"
      }
      """.trimIndent()

  @Test
  fun `can get product from open products facts when not in food facts`() {
    val ean = "1234567890123"
    openFoodFactsApi.stubGetProduct(
      ean,
      """
      {
        "code": "$ean",
        "status": 0,
        "status_verbose": "product not found"
      }
      """.trimIndent(),
    )
    openProductsFactsApi.stubGetProduct(
      ean,
      """
      {
        "code": "$ean",
        "product": {
          "product_name": "iPhone",
          "brands": "Apple"
        },
        "status": 1,
        "status_verbose": "product found"
      }
      """.trimIndent(),
    )

    webTestClient.get()
      .uri("/api/product/$ean")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.code").isEqualTo(ean)
      .jsonPath("$.product.product_name").isEqualTo("iPhone")
      .jsonPath("$.status").isEqualTo(1)
  }

  @Test
  fun `returns empty if product not found in either API`() {
    val ean = "0000000000000"
    openFoodFactsApi.stubGetProduct(
      ean,
      """
      {
        "code": "$ean",
        "status": 0,
        "status_verbose": "product not found"
      }
      """.trimIndent(),
    )
    openProductsFactsApi.stubGetProduct(
      ean,
      """
      {
        "code": "$ean",
        "status": 0,
        "status_verbose": "product not found"
      }
      """.trimIndent(),
    )

    webTestClient.get()
      .uri("/api/product/$ean")
      .exchange()
      .expectStatus().isOk
      .expectBody().isEmpty
  }

  @Test
  fun `can get product from open products facts when food facts returns 404`() {
    val ean = "0035000444349"
    openFoodFactsApi.stubGetProductNotFound(ean)
    openProductsFactsApi.stubGetProduct(ean, bakingSodaWhiteningResponse(ean))

    webTestClient.get()
      .uri("/api/product/$ean")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.code").isEqualTo(ean)
      .jsonPath("$.product.product_name").isEqualTo("Baking soda & peroxide whitening")
      .jsonPath("$.status").isEqualTo(1)
  }

  private fun bakingSodaWhiteningResponse(ean: String) = """
      {
        "code": "$ean",
        "product": {
          "product_name": "Baking soda & peroxide whitening",
          "brands": "Colgate",
          "quantity": "4 oz",
          "image_url": "https://images.openproductsfacts.org/images/products/003/500/044/4349/front_en.10.400.jpg",
          "allergens": "",
          "ingredients_analysis_tags": null,
          "packaging_tags": null,
          "categories_tags": [
              "health-beauty",
              "personal-care",
              "oral-care",
              "toothpaste"
          ],
          "nutriments": null
        },
        "status": 1,
        "status_verbose": "product found"
      }
      """.trimIndent()
}
