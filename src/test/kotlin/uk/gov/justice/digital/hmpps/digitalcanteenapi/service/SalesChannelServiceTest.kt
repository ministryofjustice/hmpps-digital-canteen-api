package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import io.jsonwebtoken.lang.Collections.setOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.SALES_CHANNEL_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.matchedProducts
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.BatchProductsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.SalesChannelService

@ExtendWith(MockitoExtension::class)
class SalesChannelServiceTest {

  @Mock
  private lateinit var medusaProductService: MedusaProductService

  @InjectMocks
  private lateinit var service: SalesChannelService

  @BeforeEach
  fun setup() {
    service = SalesChannelService(medusaProductService)
  }

  @Test
  fun `should add new products and remove obsolete products from sales channel`() {
    // Given
    whenever(
      medusaProductService.getSalesChannelProductIds(SALES_CHANNEL_ID),
    ).thenReturn(
      setOf(
        "product-2",
        "product-3",
      ),
    )

    // When
    service.syncProduct(SALES_CHANNEL_ID, matchedProducts)

    // Then
    argumentCaptor<BatchProductsRequest>().apply {
      verify(medusaProductService).updateSalesChannelProducts(
        salesChannelId = eq(SALES_CHANNEL_ID),
        request = capture(),
      )

      assertThat(firstValue.add)
        .containsExactly("product-1")

      assertThat(firstValue.remove)
        .containsExactly("product-3")
    }
  }

  @Test
  fun `should not add or remove products when sales channel already contains correct products`() {
    // Given
    whenever(
      medusaProductService.getSalesChannelProductIds(SALES_CHANNEL_ID),
    ).thenReturn(
      setOf(
        "product-1",
        "product-2",
      ),
    )

    // When
    service.syncProduct(SALES_CHANNEL_ID, matchedProducts)

    // Then
    argumentCaptor<BatchProductsRequest>().apply {
      verify(medusaProductService).updateSalesChannelProducts(
        salesChannelId = eq(SALES_CHANNEL_ID),
        request = capture(),
      )
      assertThat(firstValue.add).isNullOrEmpty()
      assertThat(firstValue.remove).isNullOrEmpty()
    }
  }

  @Test
  fun `should add new products if sales channel has no products associated`() {
    // Given
    whenever(
      medusaProductService.getSalesChannelProductIds(SALES_CHANNEL_ID),
    ).thenReturn(
      emptySet(),
    )
    // When
    service.syncProduct(SALES_CHANNEL_ID, matchedProducts)

    // Then
    argumentCaptor<BatchProductsRequest>().apply {
      verify(medusaProductService).updateSalesChannelProducts(
        salesChannelId = eq(SALES_CHANNEL_ID),
        request = capture(),
      )
      assertThat(firstValue.add)
        .containsExactly("product-1", "product-2")
    }
  }
}
