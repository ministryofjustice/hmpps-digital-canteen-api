package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.CreateProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.DeletedResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductReference
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.UpdateProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.nplProductMapper.ProductMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.MedusaProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importnpl.NplProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importNPL.NationalProductListParseService
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importNPL.NationalProductListSyncService
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.util.Collections.emptyList

@ExtendWith(MockitoExtension::class)
class NationalProductListSyncServiceTest {

  @Mock
  lateinit var parser: NationalProductListParseService

  @Mock
  lateinit var medusaProductService: MedusaProductService

  @Mock
  lateinit var productMapper: ProductMapper

  @Mock
  lateinit var file: MultipartFile

  @InjectMocks
  lateinit var service: NationalProductListSyncService

  @Test
  fun `should create news products`() {
    // Given
    val uploadedProduct = NplProduct(
      icCode = "IC001",
      shortDescription = "Product 1",
      description = "Description",
      eanBarcode = "123456",
      lplOrderingCode = "ABC",
      pricePence = BigDecimal("100"),
      selectionCategories = "CAT",
    )

    whenever(file.inputStream)
      .thenReturn(ByteArrayInputStream(ByteArray(0)))

    whenever(parser.parse(any()))
      .thenReturn(listOf(uploadedProduct))

    whenever(
      medusaProductService.getAllProducts(),
    ).thenReturn(emptyList())

    val createRequest = mock<CreateProduct>()
    whenever(productMapper.toCreateProduct(uploadedProduct))
      .thenReturn(createRequest)

    val expectedResponse = ProductBatchResponse(
      created = listOf(ProductReference(id = "1")),
      updated = emptyList(),
      deleted = DeletedResponse(
        ids = emptyList(),
        deleted = false,
      ),
    )

    whenever(medusaProductService.batchProducts(any()))
      .thenReturn(expectedResponse)

    // When
    val response = service.syncProducts(file)

    // Then
    verify(parser).parse(any())

    verify(productMapper).toCreateProduct(uploadedProduct)

    val requestCaptor = argumentCaptor<ProductBatchRequest>()

    verify(medusaProductService)
      .batchProducts(requestCaptor.capture())

    with(requestCaptor.firstValue) {
      assertEquals(1, create?.size)
      assertEquals(0, update?.size)
      assertEquals(0, delete?.size)
    }

    assertEquals(expectedResponse, response)
  }

  @Test
  fun `should update existing product when product has changed`() {
    // Given
    val uploadedProduct = NplProduct(
      icCode = "IC001",
      shortDescription = "Product New",
      description = "Product New",
      eanBarcode = "123456",
      lplOrderingCode = "ABC",
      pricePence = BigDecimal("100"),
      selectionCategories = "CAT",
    )

    val existingProduct = MedusaProduct(
      id = "1",
      handle = "IC001",
      title = "Product Old",
      description = "Product Old",
      subtitle = "Product Old",
      metadata = uk.gov.justice.digital.hmpps.digitalcanteenapi.model.ProductMetadata(
        lplOrderingCode = "ABC",
        eanBarcode = "123456",
        selectionCategory = "CAT",
      ),
    )

    val updateProduct = UpdateProduct(
      id = "1",
      variants = emptyList(),
      title = "Product New",
      description = "Product New",
      subtitle = "Product New",
      metadata = ProductMetadata(
        lplOrderingCode = "ABC",
        eanBarcode = "123456",
        selectionCategory = "CAT",
      ),
      handle = "IC001",
      status = "Published",
    )

    whenever(file.inputStream)
      .thenReturn(ByteArrayInputStream(ByteArray(0)))

    whenever(parser.parse(any()))
      .thenReturn(listOf(uploadedProduct))

    whenever(medusaProductService.getAllProducts())
      .thenReturn(listOf(existingProduct))

    whenever(
      productMapper.toUpdateProduct(
        uploadedProduct,
        "1",
      ),
    ).thenReturn(updateProduct)

    val expectedResponse = ProductBatchResponse(
      created = emptyList(),
      updated = listOf(ProductReference(id = "1")),
      deleted = DeletedResponse(
        ids = emptyList(),
        deleted = false,
      ),
    )

    whenever(medusaProductService.batchProducts(any()))
      .thenReturn(expectedResponse)

    // When
    val response = service.syncProducts(file)

    // Then
    verify(productMapper)
      .toUpdateProduct(uploadedProduct, "1")

    val captor = argumentCaptor<ProductBatchRequest>()

    verify(medusaProductService)
      .batchProducts(captor.capture())

    with(captor.firstValue) {
      assertEquals(0, create?.size)
      assertEquals(1, update?.size)
      assertEquals(0, delete?.size)
    }

    assertEquals(expectedResponse, response)
  }

  @Test
  fun `should create a new product and delete products that are not in uploaded file`() {
    // Given
    val uploadedProduct = NplProduct(
      icCode = "IC002",
      shortDescription = "Product New",
      description = "Product New",
      eanBarcode = "123456",
      lplOrderingCode = "ABC",
      pricePence = BigDecimal("100"),
      selectionCategories = "CAT",
    )

    val existingProduct = MedusaProduct(
      id = "1",
      handle = "IC001",
      title = "Product Old",
      description = "Product Old",
      subtitle = "Product Old",
      metadata = uk.gov.justice.digital.hmpps.digitalcanteenapi.model.ProductMetadata(
        lplOrderingCode = "ABC",
        eanBarcode = "123456",
        selectionCategory = "CAT",
      ),
    )

    whenever(file.inputStream)
      .thenReturn(ByteArrayInputStream(ByteArray(0)))

    whenever(parser.parse(any()))
      .thenReturn(listOf(uploadedProduct))

    whenever(medusaProductService.getAllProducts())
      .thenReturn(listOf(existingProduct))

    val expectedResponse = ProductBatchResponse(
      created = emptyList(),
      updated = listOf(ProductReference(id = "1")),
      deleted = DeletedResponse(
        ids = listOf(existingProduct.id),
        deleted = true,
      ),
    )

    whenever(medusaProductService.batchProducts(any()))
      .thenReturn(expectedResponse)

    // When
    val response = service.syncProducts(file)

    // Then
    verify(productMapper, never())
      .toUpdateProduct(any(), any())

    val captor = argumentCaptor<ProductBatchRequest>()

    verify(medusaProductService)
      .batchProducts(captor.capture())

    with(captor.firstValue) {
      assertEquals(1, create?.size)
      assertEquals(0, update?.size)
      assertEquals(1, delete?.size)
    }

    assertEquals(expectedResponse, response)
  }

  @Test
  fun `should delete products that are not in uploaded file`() {
    // Given
    val uploadedProduct = NplProduct(
      icCode = "IC001",
      shortDescription = "Product Old",
      description = "Product Old",
      eanBarcode = "123456",
      lplOrderingCode = "ABC",
      pricePence = BigDecimal("100"),
      selectionCategories = "CAT",
    )

    val existingProduct1 = MedusaProduct(
      id = "1",
      handle = "IC001",
      title = "Product Old",
      description = "Product Old",
      subtitle = "Product Old",
      metadata = uk.gov.justice.digital.hmpps.digitalcanteenapi.model.ProductMetadata(
        lplOrderingCode = "ABC",
        eanBarcode = "123456",
        selectionCategory = "CAT",
      ),
    )

    val existingProduct2 = MedusaProduct(
      id = "2",
      handle = "IC002",
      title = "Product Old",
      description = "Product Old",
      subtitle = "Product Old",
      metadata = uk.gov.justice.digital.hmpps.digitalcanteenapi.model.ProductMetadata(
        lplOrderingCode = "ABC",
        eanBarcode = "123456",
        selectionCategory = "CAT",
      ),
    )

    whenever(file.inputStream)
      .thenReturn(ByteArrayInputStream(ByteArray(0)))

    whenever(parser.parse(any()))
      .thenReturn(listOf(uploadedProduct))

    whenever(medusaProductService.getAllProducts())
      .thenReturn(listOf(existingProduct1, existingProduct2))

    val expectedResponse = ProductBatchResponse(
      created = emptyList(),
      updated = listOf(ProductReference(id = "1")),
      deleted = DeletedResponse(
        ids = listOf(existingProduct1.id),
        deleted = true,
      ),
    )

    whenever(medusaProductService.batchProducts(any()))
      .thenReturn(expectedResponse)

    // When
    val response = service.syncProducts(file)

    // Then
    verify(productMapper, never())
      .toCreateProduct(any())

    verify(productMapper, never())
      .toUpdateProduct(any(), any())

    val captor = argumentCaptor<ProductBatchRequest>()

    verify(medusaProductService)
      .batchProducts(captor.capture())

    with(captor.firstValue) {
      assertEquals(0, create?.size)
      assertEquals(0, update?.size)
      assertEquals(1, delete?.size)
    }

    assertEquals(expectedResponse, response)
  }

  @Test
  fun `should not update product when no changes are detected`() {
    // Given
    val uploadedProduct = NplProduct(
      icCode = "IC001",
      shortDescription = "Product 1",
      description = "Product 1",
      eanBarcode = "123456",
      lplOrderingCode = "ABC",
      pricePence = BigDecimal("100"),
      selectionCategories = "CAT",
    )

    val existingProduct = MedusaProduct(
      id = "1",
      handle = "IC001",
      title = "Product 1",
      description = "Product 1",
      subtitle = "Product 1",
      metadata = uk.gov.justice.digital.hmpps.digitalcanteenapi.model.ProductMetadata(
        lplOrderingCode = "ABC",
        eanBarcode = "123456",
        selectionCategory = "CAT",
      ),
    )

    whenever(file.inputStream)
      .thenReturn(ByteArrayInputStream(ByteArray(0)))

    whenever(parser.parse(any()))
      .thenReturn(listOf(uploadedProduct))

    whenever(medusaProductService.getAllProducts())
      .thenReturn(listOf(existingProduct))

    val expectedResponse = ProductBatchResponse(
      created = emptyList(),
      updated = emptyList(),
      deleted = DeletedResponse(
        ids = emptyList(),
        deleted = true,
      ),
    )

    whenever(medusaProductService.batchProducts(any()))
      .thenReturn(expectedResponse)

    // When
    val response = service.syncProducts(file)

    // Then
    verify(productMapper, never())
      .toCreateProduct(any())

    verify(productMapper, never())
      .toUpdateProduct(any(), any())

    val captor = argumentCaptor<ProductBatchRequest>()

    verify(medusaProductService)
      .batchProducts(captor.capture())

    with(captor.firstValue) {
      assertEquals(0, create?.size)
      assertEquals(0, update?.size)
      assertEquals(0, delete?.size)
    }

    assertEquals(expectedResponse, response)
  }
}
