package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.localproductlistimportapi.generated.ImportLocalProductListResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.PRISON_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.localProduct1
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.localProduct2
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.matchedProducts
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.medusaProduct1
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.mockFile
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.region
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.salesChannel
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.ProductMatchingResult
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.LocalProductListParseService
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.LocalProductListSyncService
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.PriceListService
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.ProductMatchService
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.SalesChannelService
import java.io.InputStream

@ExtendWith(MockitoExtension::class)
class LocalProductListSyncServiceTest {
  @Mock
  lateinit var localProductListParseService: LocalProductListParseService

  @Mock
  lateinit var medusaProductService: MedusaProductService

  @Mock
  lateinit var productMatchService: ProductMatchService

  @Mock
  lateinit var salesChannelService: SalesChannelService

  @Mock
  lateinit var priceListService: PriceListService

  @InjectMocks
  lateinit var service: LocalProductListSyncService

  @Test
  fun `should sync products and create price list successfully`() {
    val localProducts = listOf(localProduct1, localProduct2)
    val medusaProducts = listOf(medusaProduct1)

    val unmatchedProducts = listOf("PR2")

    whenever(localProductListParseService.parse(any()))
      .thenReturn(localProducts)

    whenever(medusaProductService.getAllProducts())
      .thenReturn(medusaProducts)

    whenever(medusaProductService.getRegions(PRISON_ID))
      .thenReturn(region)

    whenever(medusaProductService.getSalesChannel(PRISON_ID))
      .thenReturn(salesChannel)

    whenever(
      productMatchService.match(
        localProducts,
        medusaProducts,
      ),
    ).thenReturn(
      ProductMatchingResult(
        matchedProducts = matchedProducts,
        unmatchedProducts = unmatchedProducts,
      ),
    )

    val response = service.syncProducts(mockFile, PRISON_ID)

    verify(salesChannelService).syncProduct(
      salesChannel.id,
      matchedProducts,
    )

    verify(priceListService).recreatePriceList(
      prisonId = PRISON_ID,
      matchedProducts = matchedProducts,
      regionId = region.id,
    )

    assertThat(response).isEqualTo(
      ImportLocalProductListResponse(
        success = true,
        matchedProducts = 2,
        unmatchedProducts = 1,
        message = "Local product for $PRISON_ID uploaded successfully",
      ),
    )
  }

  @Test
  fun `should return failure when file parsing fails`() {
    whenever(localProductListParseService.parse(any()))
      .thenThrow(RuntimeException("Invalid File"))

    val response = service.syncProducts(mockFile, PRISON_ID)

    assertThat(response).isEqualTo(
      ImportLocalProductListResponse(
        success = false,
        matchedProducts = 0,
        unmatchedProducts = 0,
        message = "Invalid File",
      ),
    )

    verifyNoInteractions(
      medusaProductService,
      salesChannelService,
      productMatchService,
      priceListService,
    )
  }

  @Test
  fun `should return failure when uploaded file is empty`() {
    whenever(localProductListParseService.parse(any()))
      .thenReturn(emptyList())

    val response = service.syncProducts(mockFile, PRISON_ID)

    assertThat(response.success).isFalse()
    assertThat(response.message).isEqualTo("Import failed: the uploaded file is empty..")

    verifyNoMoreInteractions(
      medusaProductService,
      salesChannelService,
      productMatchService,
      priceListService,
    )
  }

  @Test
  fun `should return failure when no products exist in medusa`() {
    whenever(localProductListParseService.parse(any()))
      .thenReturn(listOf(localProduct1))

    whenever(medusaProductService.getAllProducts())
      .thenReturn(emptyList())

    val response = service.syncProducts(mockFile, PRISON_ID)

    assertThat(response.success).isFalse()
    assertThat(response.message).isEqualTo("Import failed: the Medusa product is empty")

    verifyNoInteractions(
      salesChannelService,
      productMatchService,
      priceListService,
    )
  }

  @Test
  fun `should return failure when region lookup fails`() {
    whenever(localProductListParseService.parse(any()))
      .thenReturn(listOf(localProduct1))

    whenever(medusaProductService.getAllProducts())
      .thenReturn(listOf(medusaProduct1))

    whenever(medusaProductService.getRegions(any()))
      .thenThrow(RuntimeException())

    val response = service.syncProducts(mockFile, PRISON_ID)

    assertThat(response.success).isFalse()
    assertThat(response.message).contains("Failed to upload")
  }

  @Test
  fun `should return failure when sales channel lookup fails`() {
    whenever(localProductListParseService.parse(any()))
      .thenReturn(listOf(localProduct1))

    whenever(medusaProductService.getAllProducts())
      .thenReturn(listOf(medusaProduct1))

    whenever(medusaProductService.getRegions(any()))
      .thenReturn(region)

    whenever(medusaProductService.getSalesChannel(any()))
      .thenThrow(RuntimeException())

    val response = service.syncProducts(mockFile, PRISON_ID)

    assertThat(response.success).isFalse()
    assertThat(response.message).contains("Failed to upload")
  }

  @Test
  fun `should execute workflow in correct order`() {
    // Given
    val localProducts = listOf(localProduct1, localProduct2)
    val medusaProducts = listOf(medusaProduct1)

    val matchResult = ProductMatchingResult(
      matchedProducts = matchedProducts,
      unmatchedProducts = emptyList(),
    )

    whenever(localProductListParseService.parse(any<InputStream>()))
      .thenReturn(localProducts)

    whenever(medusaProductService.getAllProducts())
      .thenReturn(medusaProducts)

    whenever(medusaProductService.getRegions(PRISON_ID))
      .thenReturn(region)

    whenever(medusaProductService.getSalesChannel(PRISON_ID))
      .thenReturn(salesChannel)

    whenever(
      productMatchService.match(
        localProducts,
        medusaProducts,
      ),
    ).thenReturn(matchResult)

    service.syncProducts(mockFile, PRISON_ID)

    inOrder(
      localProductListParseService,
      medusaProductService,
      productMatchService,
      salesChannelService,
      priceListService,
    ).apply {
      verify(localProductListParseService).parse(any())
      verify(medusaProductService).getAllProducts()
      verify(medusaProductService).getRegions(any())
      verify(medusaProductService).getSalesChannel(any())
      verify(productMatchService)
        .match(any(), any())
      verify(salesChannelService)
        .syncProduct(any(), any())
      verify(priceListService)
        .recreatePriceList(any(), any(), any())
    }
  }
}
