package uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.localproductlistimportapi.generated.ImportLocalProductListResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MedusaRegion
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MedusaSalesChannel
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.MedusaProductService

@Service
class LocalProductListSyncService(
  private val parser: LocalProductListParseService,
  private val medusaProductService: MedusaProductService,
  private val productMatchService: ProductMatchService,
  private val salesChannelService: SalesChannelService,
  private val priceListService: PriceListService,
) {
  companion object {
    val logger: Logger = LoggerFactory.getLogger(LocalProductListSyncService::class.java)
  }

  fun syncProducts(file: MultipartFile, prisonId: String): ImportLocalProductListResponse {
    val lplProducts = try {
      parser.parse(file.inputStream)
    } catch (e: Exception) {
      logger.error("Failed to parse uploaded file: {}", e.message)

      return ImportLocalProductListResponse(
        success = false,
        matchedProducts = 0,
        unmatchedProducts = 0,
        message = e.message ?: "Failed to parse uploaded file",
      )
    }

    if (lplProducts.isEmpty()) {
      logger.warn("No products found in uploaded file")
      return ImportLocalProductListResponse(
        success = false,
        matchedProducts = 0,
        unmatchedProducts = 0,
        message = "Import failed: the uploaded file is empty..",
      )
    }
    val medusaProducts = medusaProductService.getAllProducts()

    if (medusaProducts.isEmpty()) {
      logger.warn("No products returned from Medusa")
      return ImportLocalProductListResponse(
        success = false,
        matchedProducts = 0,
        unmatchedProducts = lplProducts.size,
        message = "Import failed: the Medusa product is empty",
      )
    }

    val region: MedusaRegion
    val salesChannel: MedusaSalesChannel

    try {
      region = medusaProductService.getRegions(prisonId)
      salesChannel = medusaProductService.getSalesChannel(prisonId)
    } catch (e: Exception) {
      logger.warn("Prison configuration missing for prison {} : {}", prisonId, e.message)

      return ImportLocalProductListResponse(
        success = false,
        matchedProducts = 0,
        unmatchedProducts = 0,
        message = e.message ?: "Failed to upload the file",
      )
    }

    val result = productMatchService.match(
      lplProducts,
      medusaProducts,
    )
    salesChannelService.syncProduct(salesChannel.id, result.matchedProducts)

    priceListService.recreatePriceList(prisonId, result.matchedProducts, region.id)
    return ImportLocalProductListResponse(
      success = true,
      matchedProducts = result.matchedProducts.size,
      unmatchedProducts = result.unmatchedProducts.size,
      message = "Local product for $prisonId uploaded successfully",
    )
  }
}
