package uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.MedusaProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.LplProductDetails
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MatchedProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.ProductMatchingResult
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.LocalProductListSyncService

@Service
class ProductMatchService {

  fun match(lplProducts: List<LplProductDetails>, medusaProducts: List<MedusaProduct>): ProductMatchingResult {
    val productsByHandle = medusaProducts
      .filter { !it.handle.isNullOrBlank() }
      .associateBy { requireNotNull(it.handle).trim() }

    val unmatchedProducts = mutableListOf<String>()

    val matchedProducts = lplProducts.mapNotNull { lplProduct ->
      val medusaProduct = productsByHandle[
        lplProduct.productCode.trim(),
      ]

      if (medusaProduct == null) {
        unmatchedProducts.add(lplProduct.productCode)
        return@mapNotNull null
      }

      val variantId = medusaProduct.variants
        .firstOrNull()
        ?.id
        ?: run {
          unmatchedProducts.add(lplProduct.productCode)
          return@mapNotNull null
        }

      MatchedProduct(
        productId = medusaProduct.id,
        productCode = lplProduct.productCode,
        variantId = variantId,
        priceInPence = lplProduct.price,
      )
    }

    LocalProductListSyncService.Companion.logger.info(
      "LPL products={}, matched={}, unmatched={}",
      lplProducts.size,
      matchedProducts.size,
      unmatchedProducts.size,
    )

    return ProductMatchingResult(
      matchedProducts = matchedProducts,
      unmatchedProducts = unmatchedProducts,
    )
  }
}
