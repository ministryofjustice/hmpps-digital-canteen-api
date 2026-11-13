package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.ProductListResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.BatchProductsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.CreatePriceListDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.CreatePriceListResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MedusaRegion
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MedusaRegionsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MedusaSalesChannel
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.PriceList
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.PriceListResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.SalesChannelProductsResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.SalesChannelsResponse

@Component
class MedusaProductClient(
  @Qualifier("medusaAdminWebClient")
  private val webClient: WebClient,
  private val errorHandler: WebClientErrorHandler,
  private val objectMapper: ObjectMapper,
) {
  companion object {
    private const val PRODUCTS_URI = "/admin/products"
    private const val BATCH_PRODUCTS_URI = "/admin/products/batch"
    val logger: Logger = LoggerFactory.getLogger(MedusaProductClient::class.java)
  }

  fun getProducts(
    limit: Int,
    offset: Int,
  ): ProductListResponse = webClient
    .get()
    .uri {
      it.path(PRODUCTS_URI)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .build()
    }
    .retrieve()
    .onStatus(HttpStatusCode::isError) { response ->
      response
        .bodyToMono(String::class.java)
        .map { body ->
          UpstreamException(
            "Failed to fetch products. Status=${response.statusCode()}, Body=$body",
          )
        }
    }
    .bodyToMono(ProductListResponse::class.java)
    .block()
    ?: throw UpstreamException(
      "Medusa returned an empty response when fetching products",
    )

  fun batchProducts(request: ProductBatchRequest): ProductBatchResponse = webClient.post()
    .uri(BATCH_PRODUCTS_URI)
    .bodyValue(request)
    .retrieve()
    .bodyToMono(ProductBatchResponse::class.java)
    .onErrorMap(WebClientResponseException::class.java) { ex ->
      if (ex.statusCode.is5xxServerError) {
        UpstreamException("Medusa service is currently unavailable")
      } else {
        logger.error("Batch update request failed: ${ex.responseBodyAsString}")
        val message = objectMapper.readTree(ex.responseBodyAsString)
          .path("message")
          .asText(ex.message)
        UpstreamException(message ?: "National Product List failed")
      }
    }
    .onErrorMap(WebClientRequestException::class.java) { ex ->
      logger.error("Upload NPL failed due to connection issue", ex)
      UpstreamException("Medusa service is currently unavailable")
    }
    .block()!!

  fun getRegions(prisonCode: String): MedusaRegion = webClient
    .get()
    .uri("/admin/regions")
    .retrieve()
    .bodyToMono(MedusaRegionsResponse::class.java)
    .block()
    ?.regions
    ?.firstOrNull {
      it.metadata?.get("prison_code")?.toString() == prisonCode
    }
    ?: throw ResourceNotFoundException("The selected prison is not fully configured in the system. Please contact support team.")

  fun getSalesChannel(prisonCode: String): MedusaSalesChannel = webClient
    .get()
    .uri("/admin/sales-channels")
    .retrieve()
    .bodyToMono(SalesChannelsResponse::class.java)
    .block()
    ?.salesChannels
    ?.firstOrNull {
      it.name.equals(prisonCode, ignoreCase = true)
    }
    ?: throw ResourceNotFoundException("The selected prison is not fully configured in the system. Please contact support team.")

  fun getSalesChannelProductIds(salesChannelId: String, limit: Int, offset: Int): SalesChannelProductsResponse = webClient
    .get()
    .uri {
      it.path("/admin/products")
        .queryParam("sales_channel_id", salesChannelId)
        .queryParam("limit", limit)
        .queryParam("offset", offset)
        .build()
    }
    .retrieve()
    .onStatus(HttpStatusCode::isError) { response ->
      response
        .bodyToMono(String::class.java)
        .map { body ->
          UpstreamException(
            "Failed to fetch products. Status=${response.statusCode()}, Body=$body",
          )
        }
    }
    .bodyToMono(SalesChannelProductsResponse::class.java)
    .block()
    ?: throw UpstreamException(
      "Medusa returned an empty response when fetching products",
    )

  fun findPriceList(prisonCode: String): PriceList? = try {
    webClient
      .get()
      .uri("/admin/price-lists")
      .retrieve()
      .bodyToMono(PriceListResponse::class.java)
      .block()
      ?.priceLists
      ?.firstOrNull {
        it.metadata?.prisonId.equals(
          prisonCode,
          ignoreCase = true,
        )
      }
  } catch (e: Exception) {
    throw IllegalStateException(
      "Failed to retrieve price lists for prison code: $prisonCode",
      e,
    )
  }

  fun deletePriceList(priceListId: String) = webClient
    .delete()
    .uri("/admin/price-lists/$priceListId")
    .retrieve()
    .onStatus(HttpStatusCode::isError) { response ->
      response.bodyToMono(String::class.java)
        .map { body ->
          UpstreamException(
            "Failed to delete price list. Status=${response.statusCode()}, Body=$body",
          )
        }
    }
    .toBodilessEntity()
    .block()

  fun updateSalesChannelProducts(salesChannelId: String, request: BatchProductsRequest) = webClient
    .post()
    .uri("/admin/sales-channels/$salesChannelId/products")
    .bodyValue(request)
    .retrieve()
    .toBodilessEntity()
    .block()

  fun createPriceList(priceList: CreatePriceListDto, prisonId: String): PriceList = webClient
    .post()
    .uri("/admin/price-lists")
    .bodyValue(priceList)
    .retrieve()
    .bodyToMono(CreatePriceListResponse::class.java)
    .block()
    ?.priceList
    ?: throw UpstreamException("Failed to create price list for prison: $prisonId")
}
