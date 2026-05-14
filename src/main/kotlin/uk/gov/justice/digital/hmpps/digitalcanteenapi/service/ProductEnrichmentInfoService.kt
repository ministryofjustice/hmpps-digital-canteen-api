package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.dto.ProductDetailsResponse

@Service
class ProductEnrichmentInfoService(
  private val openFoodFactsWebClient: WebClient,
  private val openProductsFactsWebClient: WebClient,
) {
  private val log = LoggerFactory.getLogger(ProductEnrichmentInfoService::class.java)

  companion object {
    private const val MIN_TAG_LENGTH = 3
  }

  fun getProductEnrichmentDetails(ean: String): Mono<ProductDetailsResponse> {
    return getProductEnrichmentDetailsFromClient(
      openFoodFactsWebClient,
      ean,
      "OpenFoodFacts",
    ) // First try OpenFoodFacts
      .switchIfEmpty(
        getProductEnrichmentDetailsFromClient(
          openProductsFactsWebClient,
          ean,
          "OpenProductsFacts",
        ),
      ) // Then try OpenProductsFacts
  }

  private fun getProductEnrichmentDetailsFromClient(
    client: WebClient,
    ean: String,
    clientName: String,
  ): Mono<ProductDetailsResponse> {
    return client.get()
      .uri("/api/v0/product/{ean}.json", ean)
      .retrieve()
      .bodyToMono(ProductDetailsResponse::class.java)
      .flatMap { response ->
        // Only condition that triggers fallback
        if (response.status == 0) {
          log.info(
            "Product not found (status 0) from {} for ean {}. status_verbose={}",
            clientName,
            ean,
            response.statusVerbose,
          )
          return@flatMap Mono.empty()
        }

        // Valid product → clean and return
        Mono.just(cleanResponse(response))
      }.onErrorResume { e ->
        log.info("Error retrieving product from {} for ean {}: {}", clientName, ean, e.message)
        Mono.empty()
      }
  }

  private fun cleanResponse(response: ProductDetailsResponse): ProductDetailsResponse {
    val product = response.product ?: return response

    fun List<String>?.cleanTags() = this
      ?.map { it.trim() }
      ?.filter { it.startsWith("en:") && it.length > MIN_TAG_LENGTH }
      ?.map { it.removePrefix("en:") }

    fun String?.cleanAllergens() = this
      ?.split(",")
      ?.map { it.trim() }
      ?.filter { it.startsWith("en:") && it.length > MIN_TAG_LENGTH }
      ?.joinToString(",") { it.removePrefix("en:") }

    return response.copy(
      product = product.copy(
        ingredientsAnalysisTags = product.ingredientsAnalysisTags.cleanTags(),
        packagingTags = product.packagingTags.cleanTags(),
        categoriesTags = product.categoriesTags.cleanTags(),
        allergens = product.allergens.cleanAllergens(),
      ),
    )
  }
}
