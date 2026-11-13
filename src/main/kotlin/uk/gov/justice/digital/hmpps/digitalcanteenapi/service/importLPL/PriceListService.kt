package uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.CreatePriceListDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MatchedProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.PriceListMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.PriceListPrice
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.MedusaProductService
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.LocalProductListSyncService

@Service
class PriceListService(
  private val medusaProductService: MedusaProductService,
) {

  fun recreatePriceList(prisonId: String, matchedProducts: List<MatchedProduct>, regionId: String) {
    medusaProductService.findPriceList(prisonId)?.let {
      LocalProductListSyncService.Companion.logger.info("Found PriceList for prison {}, now deleting it.", prisonId)
      medusaProductService.deletePriceList(it.id)
    }

    val priceList = CreatePriceListDto(
      title = "$prisonId Price List",
      description = "$prisonId Price List",
      status = "active",
      metadata = PriceListMetadata(
        prisonId = prisonId,
      ),
      prices = matchedProducts.map { product ->
        PriceListPrice(
          variantId = product.variantId,
          amount = product.priceInPence!!,
          currencyCode = "gbp",
          rules = mapOf("region_id" to regionId),
        )
      },
    )
    medusaProductService.createPriceList(priceList, prisonId)
  }
}
