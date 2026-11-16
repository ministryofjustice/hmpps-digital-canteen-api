package uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures

import org.springframework.mock.web.MockMultipartFile
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.MedusaProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.MedusaVariant
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.ProductMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.LplProductDetails
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MatchedProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MedusaRegion
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MedusaSalesChannel
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.PriceList
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.PriceListMetadata
import java.util.Collections.emptyMap

object TestMockData {

  val mockFile: MockMultipartFile = MockMultipartFile(
    "file",
    "MDI_lpl.csv",
    "text/csv",
    """
    CODE,DESCRIPTION,COST
    PR1,Coffee,120,
    PR2,Coca Cola,200,
    """.trimIndent().toByteArray(),
  )

  const val PRISON_ID = "MDI"

  val region = MedusaRegion(
    id = "MDI",
    name = "MDI",
    currencyCode = "gbp",
    metadata = emptyMap(),
  )

  val salesChannel = MedusaSalesChannel(
    id = "MDI",
    name = "MDI",
    description = "MDI Description",
  )

  val localProduct1 = LplProductDetails(
    title = "Coffee",
    price = 120,
    productCode = "PR1",
  )

  val localProduct2 = LplProductDetails(
    title = "Coca cola",
    price = 200,
    productCode = "PR2",
  )

  val medusaProduct1 = MedusaProduct(
    id = "Prod1",
    handle = "PR1",
    title = "Coffee",
    subtitle = "Coffee",
    description = "Hot Drink",
    variants = listOf(
      MedusaVariant(
        id = "variant-1",
      ),
    ),
    metadata = ProductMetadata(
      lplOrderingCode = "LPL6789",
      eanBarcode = "1234567890",
      selectionCategory = "DRINKS",
    ),
  )

  val medusaProduct2 = MedusaProduct(
    id = "Prod2",
    handle = "M1234",
    title = "Tea",
    subtitle = "Tea",
    description = "Hot Drink",
    variants = listOf(
      MedusaVariant(
        id = "variant-1",
      ),
    ),
    metadata = ProductMetadata(
      lplOrderingCode = "LPL5678",
      eanBarcode = "1234567890",
      selectionCategory = "DRINKS",
    ),
  )

  val matchedProducts: List<MatchedProduct> = listOf(
    MatchedProduct(
      productId = "product-1",
      variantId = "variant-1",
      productCode = "1",
      priceInPence = 100,
    ),
    MatchedProduct(
      productId = "product-2",
      variantId = "variant-2",
      productCode = "2",
      priceInPence = 200,
    ),
  )

  val existingPriceList = PriceList(
    id = "price-list-id",
    title = "MDI Price list",
    type = "override",
    metadata = PriceListMetadata(PRISON_ID),
  )

  const val SALES_CHANNEL_ID = "channel-1"
}
