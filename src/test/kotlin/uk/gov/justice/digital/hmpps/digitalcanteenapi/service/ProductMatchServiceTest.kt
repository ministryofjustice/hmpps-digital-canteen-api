package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.aot.hint.TypeReference.listOf
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.localProduct1
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.localProduct2
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.medusaProduct1
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.medusaProduct2
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.ProductMatchService

@ExtendWith(MockitoExtension::class)
class ProductMatchServiceTest {

  @InjectMocks
  private lateinit var service: ProductMatchService

  @BeforeEach
  fun setup() {
    service = ProductMatchService()
  }

  @Test
  fun `should return matched and unmatched products list`() {
    // Given
    val medusaProductList = listOf(medusaProduct1, medusaProduct2)
    val localProductList = listOf(localProduct1, localProduct2)

    val result = service.match(localProductList, medusaProductList)

    assertEquals(result.matchedProducts.size, 1)
    assertEquals(result.unmatchedProducts.size, 1)
  }

  @Test
  fun `should return match products list`() {
    val medusaProductList = listOf(medusaProduct1)
    val localProductList = listOf(localProduct1)

    val result = service.match(localProductList, medusaProductList)

    assertEquals(result.matchedProducts.size, 1)
    assertEquals(result.unmatchedProducts.size, 0)
  }
}
