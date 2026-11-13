package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import io.jsonwebtoken.lang.Collections.setOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.aot.hint.TypeReference.listOf
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.DeletedResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductReference
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaProductClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.MedusaProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.ProductListResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.ProductMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.SalesChannelProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.SalesChannelProductsResponse
import java.util.Collections.emptyList

@ExtendWith(MockitoExtension::class)
class MedusaProductServiceTest {

  @Mock
  private lateinit var medusaProductClient: MedusaProductClient

  @InjectMocks
  private lateinit var medusaProductService: MedusaProductService

  @Test
  fun `getAllProducts should return products from single page`() {
    // Given
    val products = MedusaProduct(
      id = "Prod1",
      handle = "M234567",
      title = "Coffee",
      subtitle = "Coffee",
      description = "Hot Drink",
      metadata = ProductMetadata(
        lplOrderingCode = "LPL6789",
        eanBarcode = "1234567890",
        selectionCategory = "DRINKS",
      ),
    )

    whenever(
      medusaProductClient.getProducts(any(), any()),
    ).thenReturn(
      ProductListResponse(
        products = listOf(products),
      ),
    )

    // When
    val result = medusaProductService.getAllProducts()

    // Then
    assertEquals(products, result.first())

    verify(medusaProductClient).getProducts(250, 0)
  }

  @Test
  fun `getAllProducts should return products from multiple pages`() {
    // Given
    val firstPageProducts = (1..250).map {
      MedusaProduct(
        id = "prod-$it",
        handle = "M$it",
        title = "Product $it",
        subtitle = null,
        description = null,
        metadata = null,
      )
    }

    val secondPageProducts = MedusaProduct(
      id = "prod-251",
      handle = "M251",
      title = "Product 251",
      subtitle = null,
      description = null,
      metadata = null,
    )

    whenever(
      medusaProductClient.getProducts(250, 0),
    ).thenReturn(
      ProductListResponse(
        products = firstPageProducts,
      ),
    )

    whenever(
      medusaProductClient.getProducts(250, 250),
    ).thenReturn(
      ProductListResponse(
        products = listOf(secondPageProducts),
      ),
    )

    // When
    val result = medusaProductService.getAllProducts()

    // Then
    assertEquals(251, result.size)

    verify(medusaProductClient).getProducts(250, 0)
    verify(medusaProductClient).getProducts(250, 250)
    verifyNoMoreInteractions(medusaProductClient)
  }

  @Test
  fun `batchProducts should delegate request to client`() {
    // Given
    val request = ProductBatchRequest(
      create = emptyList(),
      update = emptyList(),
      delete = emptyList(),
    )

    val response = ProductBatchResponse(
      created = listOf(
        ProductReference("prod_1"),
      ),
      updated = listOf(
        ProductReference("prod_2"),
      ),
      deleted = DeletedResponse(
        ids = listOf("prod_3"),
        deleted = true,
      ),
    )

    whenever(medusaProductClient.batchProducts(request))
      .thenReturn(response)

    // When
    val result = medusaProductService.batchProducts(request)

    // Then
    assertEquals(response, result)
    verify(medusaProductClient).batchProducts(request)
  }

  @Test
  fun `should return all product ids from a single page for given sales channel Id`() {
    // Given
    val salesChannelId = "sc_123"

    whenever(
      medusaProductClient.getSalesChannelProductIds(
        salesChannelId,
        250,
        0,
      ),
    ).thenReturn(
      SalesChannelProductsResponse(
        products = listOf(
          SalesChannelProduct(id = "p1"),
          SalesChannelProduct(id = "p2"),
        ),
        limit = 250,
        count = 0,
        offset = 2,
      ),
    )

    // When
    val result = medusaProductService.getSalesChannelProductIds(salesChannelId)

    // Then
    assertEquals(setOf("p1", "p2"), result)

    verify(medusaProductClient)
      .getSalesChannelProductIds(salesChannelId, 250, 0)
  }

  @Test
  fun `should fetch all pages until less than limit returned`() {
    // Given
    val salesChannelId = "sc_123"

    val firstPage = SalesChannelProductsResponse(
      products = (1..250).map {
        SalesChannelProduct(id = "p$it")
      },
      count = 252,
      offset = 0,
      limit = 250,
    )

    val secondPage = SalesChannelProductsResponse(
      products = listOf(
        SalesChannelProduct(id = "p251"),
        SalesChannelProduct(id = "p252"),
      ),
      count = 252,
      offset = 250,
      limit = 250,
    )

    whenever(
      medusaProductClient.getSalesChannelProductIds(
        salesChannelId,
        250,
        0,
      ),
    ).thenReturn(firstPage)

    whenever(
      medusaProductClient.getSalesChannelProductIds(
        salesChannelId,
        250,
        250,
      ),
    ).thenReturn(secondPage)

    // When
    val result = medusaProductService.getSalesChannelProductIds(salesChannelId)

    // Then
    assertEquals(252, result.size)
    assertTrue(result.contains("p1"))
    assertTrue(result.contains("p252"))

    verify(medusaProductClient)
      .getSalesChannelProductIds(salesChannelId, 250, 0)

    verify(medusaProductClient)
      .getSalesChannelProductIds(salesChannelId, 250, 250)
  }

  @Test
  fun `should return empty set when no products found`() {
    whenever(
      medusaProductClient.getSalesChannelProductIds(
        "sc_123",
        250,
        0,
      ),
    ).thenReturn(
      SalesChannelProductsResponse(
        products = emptyList(),
        count = 0,
        offset = 0,
        limit = 0,
      ),
    )

    val result = medusaProductService.getSalesChannelProductIds("sc_123")

    assertTrue(result.isEmpty())
  }
}
