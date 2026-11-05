package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.aot.hint.TypeReference.listOf
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.DeletedResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductReference
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaProductClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importnpl.MedusaProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importnpl.ProductListResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importnpl.ProductMetadata
import java.util.Collections.emptyList

@ExtendWith(MockitoExtension::class)
class MedusaProductServiceTest {

  @Mock
  private lateinit var medusaProductClient: MedusaProductClient

  @InjectMocks
  private lateinit var medusaProductService: MedusaProductService

  @Test
  fun `getProducts should return products from client`() {
    // Given
    val response = ProductListResponse(
      products = listOf(
        MedusaProduct(
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

        ),
      ),
    )

    whenever(medusaProductClient.getProducts())
      .thenReturn(response)

    // When
    val result = medusaProductService.getProducts()

    // Then
    assertEquals(response, result)
    verify(medusaProductClient).getProducts()
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
}
