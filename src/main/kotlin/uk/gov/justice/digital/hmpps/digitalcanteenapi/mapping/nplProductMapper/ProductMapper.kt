package uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.nplProductMapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.CreateProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.Price
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductOption
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductVariant
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductVariant1
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.UpdateProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importnpl.NplProduct

@Component
class ProductMapper {

  fun toCreateProduct(product: NplProduct): CreateProduct = CreateProduct(
    title = product.shortDescription ?: product.description ?: product.icCode,
    handle = product.icCode,
    subtitle = product.shortDescription,
    description = product.description,
    status = "published",
    options = listOf(
      ProductOption(
        title = "Size",
        propertyValues = listOf("Default"),
      ),
    ),
    variants = listOf(
      ProductVariant(
        title = "Default",
        options = mapOf(
          "Size" to "Default",
        ),
        prices = listOf(
          Price(
            currencyCode = "gbp",
            amount = product.pricePence?.toInt() ?: 0,
          ),
        ),
      ),
    ),
    metadata = ProductMetadata(
      icCode = product.icCode,
      lplOrderingCode = product.lplOrderingCode,
      eanBarcode = product.eanBarcode,
      selectionCategory = product.selectionCategories,
    ),
  )

  fun toUpdateProduct(
    product: NplProduct,
    productId: String,
  ): UpdateProduct = UpdateProduct(
    id = productId,
    title = product.shortDescription ?: product.description ?: product.icCode,
    handle = product.icCode,
    subtitle = product.shortDescription,
    description = product.description,
    status = "published",
    variants = listOf(
      ProductVariant1(
        title = "Default",
        prices = listOf(
          Price(
            currencyCode = "gbp",
            amount = product.pricePence?.toInt() ?: 0,
          ),
        ),
      ),
    ),
    metadata = ProductMetadata(
      icCode = product.icCode,
      lplOrderingCode = product.lplOrderingCode,
      eanBarcode = product.eanBarcode,
      selectionCategory = product.selectionCategories,
    ),
  )
}
