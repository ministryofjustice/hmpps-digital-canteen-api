package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.gorules.zen_engine.kotlin.JsonBuffer
import io.gorules.zen_engine.kotlin.ZenDecision
import io.gorules.zen_engine.kotlin.ZenEngine
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.productenrichment.dto.ProductDetailsResponse
import java.time.Duration

@Service
class ProductRestrictionService(
  private val resourceLoader: ResourceLoader,
  private val prisonerEnrichmentService: PrisonerEnrichmentService,
  private val productEnrichmentInfoService: ProductEnrichmentInfoService
) {
  private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
  private lateinit var decision: ZenDecision
  private val engine = ZenEngine(null, null)
  private val eanList = listOf("3017620422003", "5012035951160", "5060868950300", "34000109418")

  @PostConstruct
  fun initialize() {
    val resource = resourceLoader.getResource("classpath:rule-decisions.json")
    val ruleJson = resource.inputStream.readBytes()
    decision = engine.createDecision(JsonBuffer(ruleJson))
  }

  fun getProductRestrictionInfo(prisonerNumber: String): Mono<ProductRestrictionResponse> {
    val prisonerMono = prisonerEnrichmentService.getEnrichedPrisoner(prisonerNumber).cache()

    return getPrisonerInfoAndProductInfo(prisonerMono)
      .flatMap { request ->
        Mono.fromCallable {
          val restrictions = evaluateProductRestriction(request)
          ProductRestrictionResult(productInfo = request.productInfo, restrictions = restrictions)
        }
      }
      .collectList()
      .zipWith(prisonerMono) { productRestrictions, prisoner ->
        ProductRestrictionResponse(
          prisonerInfo = prisoner,
          productsWithRestrictions = productRestrictions
        )
      }
  }

  private fun getPrisonerInfoAndProductInfo(prisonerMono: Mono<PrisonerEnrichmentService.EnrichedPrisonerDto>):
    Flux<ProductRestrictionRequest> {
    return Flux.fromIterable(eanList)
      // OpenFoodFacts API rate limit workaround
      .delayElements(Duration.ofMillis(300))
      .flatMap { ean ->
        val productMono = productEnrichmentInfoService.getProductEnrichmentDetails(ean)
          .onErrorResume { error ->
            println("Failed to fetch product $ean: ${error.message}")
            Mono.empty()
          }

        Mono.zip(prisonerMono, productMono) { prisoner, product ->
          ProductRestrictionRequest(
            prisonerInfo = prisoner,
            productInfo = product
          )
        }
      }
  }

  private fun evaluateProductRestriction(request: ProductRestrictionRequest): ProductRestrictionDto = runBlocking {
    val inputJson = objectMapper.writeValueAsString(request).toByteArray()
    val input = JsonBuffer(inputJson)
    val response = decision.evaluate(input, null)

    val resultJson = response.result.toString()
    objectMapper.readValue(resultJson)
  }

  data class ProductRestrictionRequest(
    val prisonerInfo: PrisonerEnrichmentService.EnrichedPrisonerDto,
    val productInfo: ProductDetailsResponse,
  )

  data class ProductRestrictionResponse(
    val prisonerInfo: PrisonerEnrichmentService.EnrichedPrisonerDto,
    val productsWithRestrictions: List<ProductRestrictionResult>
  )

  data class ProductRestrictionResult(
    val productInfo: ProductDetailsResponse,
    val restrictions: ProductRestrictionDto
  )

  data class ProductRestrictionDto(
    val restricted: Boolean,
    val restrictionType: String?,
    val reason: String?,
  )
}