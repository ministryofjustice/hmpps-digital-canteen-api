package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.nplProductMapper.ProductMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importnpl.MedusaProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importnpl.NplProduct

@Service
class NationalProductListSyncService(
  private val parser: NationalProductListParseService,
  private val medusaProductService: MedusaProductService,
  private val productMapper: ProductMapper,
) {
  companion object {
    val logger: Logger = LoggerFactory.getLogger(NationalProductListSyncService::class.java)
  }

  fun syncProducts(file: MultipartFile): ProductBatchResponse {
    val uploadedProducts = parser.parse(file.inputStream)
    val existingProducts = getAllProducts()

    val existingByHandle = existingProducts
      .filter { !it.handle.isNullOrBlank() }
      .associateBy { requireNotNull(it.handle).trim() }

    val uploadedHandles = uploadedProducts
      .map { it.icCode.trim() }
      .toSet()

    val createProducts = uploadedProducts
      .filter { it.icCode.trim() !in existingByHandle }
      .map(productMapper::toCreateProduct)

    val updateProducts = uploadedProducts
      .mapNotNull { product ->
        val handle = product.icCode.trim()
        val existing = existingByHandle[handle]
          ?: return@mapNotNull null

        if (!hasChanged(product, existing)) {
          return@mapNotNull null
        }
        productMapper.toUpdateProduct(product, existing.id)
      }

    val deleteProducts = existingProducts
      .filter { it.handle?.trim() !in uploadedHandles }
      .map { it.id }

    logger.info("Modifying medusa DB : uploadedProducts: {}, existingProducts: {}, createdProducts: {}, updateProducts: {}, deleteProducts: {}", uploadedProducts, existingProducts, createProducts.size, updateProducts.size, deleteProducts.size)

    val request = ProductBatchRequest(
      create = createProducts,
      update = updateProducts,
      delete = deleteProducts,
    )

    return medusaProductService.batchProducts(request)
  }

  private fun hasChanged(
    npl: NplProduct,
    medusa: MedusaProduct,
  ): Boolean = medusa.title != npl.shortDescription ||
    medusa.description != npl.description ||
    medusa.metadata?.eanBarcode != npl.eanBarcode ||
    medusa.metadata?.lplOrderingCode != npl.lplOrderingCode ||
    medusa.metadata?.selectionCategory != npl.selectionCategories

  fun getAllProducts(): List<MedusaProduct> {
    var products = mutableListOf<MedusaProduct>()
    var offset = 0
    val limit = 100
    while (true) {
      val response = medusaProductService.getProducts(limit, offset)
      products += response.products
      if (response.products.size < limit) {
        break
      }
      offset += limit
    }
    return products
  }
}
