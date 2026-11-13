package uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.BatchProductsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MatchedProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.MedusaProductService

@Service
class SalesChannelService(
  private val medusaProductService: MedusaProductService,
) {

  fun syncProduct(salesChannelId: String, matchedProducts: List<MatchedProduct>) {
    val newProductIds = matchedProducts
      .map(MatchedProduct::productId)
      .toSet()

    val currentProductIds = medusaProductService.getSalesChannelProductIds(salesChannelId)

    val request = BatchProductsRequest(
      add = newProductIds - currentProductIds,
      remove = currentProductIds - newProductIds,
    )
    medusaProductService.updateSalesChannelProducts(
      salesChannelId = salesChannelId,
      request,
    )
  }
}
