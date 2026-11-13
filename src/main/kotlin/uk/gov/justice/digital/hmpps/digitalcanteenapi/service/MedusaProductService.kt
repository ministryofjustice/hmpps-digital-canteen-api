package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaProductClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.MedusaProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.BatchProductsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.CreatePriceListDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.SalesChannelProduct

@Service
class MedusaProductService(
  private val medusaProductClient: MedusaProductClient,
) {

  fun batchProducts(request: ProductBatchRequest) = medusaProductClient.batchProducts(request)

  fun getRegions(prisonId: String) = medusaProductClient.getRegions(prisonId)

  fun getSalesChannel(prisonId: String) = medusaProductClient.getSalesChannel(prisonId)

  fun findPriceList(prisonCode: String) = medusaProductClient.findPriceList(prisonCode)

  fun deletePriceList(priceListId: String) = medusaProductClient.deletePriceList(priceListId)

  fun updateSalesChannelProducts(salesChannelId: String, request: BatchProductsRequest) = medusaProductClient.updateSalesChannelProducts(salesChannelId, request)

  fun createPriceList(priceList: CreatePriceListDto, prisonId: String) = medusaProductClient.createPriceList(priceList, prisonId)

  fun getAllProducts(): List<MedusaProduct> {
    var products = mutableListOf<MedusaProduct>()
    var offset = 0
    val limit = 250
    while (true) {
      val response = medusaProductClient.getProducts(limit, offset)
      products += response.products
      if (response.products.size < limit) {
        break
      }
      offset += limit
    }
    return products
  }

  fun getSalesChannelProductIds(
    salesChannelId: String,
  ): Set<String> {
    var productIds = mutableListOf<SalesChannelProduct>()
    var offset = 0
    val limit = 250

    while (true) {
      val response = medusaProductClient.getSalesChannelProductIds(salesChannelId, limit, offset)
      productIds += response.products

      if (response.products.size < limit) {
        break
      }
      offset += limit
    }

    return productIds.map { it.id }.toSet()
  }
}
