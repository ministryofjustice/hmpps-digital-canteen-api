package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.CreatePriceListDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.MatchedProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.PriceList
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.PriceListService

@ExtendWith(MockitoExtension::class)
class PriceListServiceTest {

  @Mock
  private lateinit var  medusaProductService: MedusaProductService

  @InjectMocks
  private lateinit var service: PriceListService

  @BeforeEach
  fun setup() {
    service = PriceListService(medusaProductService)
  }

  @Test
  fun `should delete existing price list and create new one`() {
    // Given
    val prisonId = "MDI"
    val regionId = "region-1"

    val existingPriceList = PriceList(
      id = "$prisonId-$regionId",
      title = "$prisonId-$regionId",
      type = "override",
      metadata  = null
    )

    val matchedProducts = listOf(
      MatchedProduct(
        productId = "product-$prisonId-$regionId",
        productCode = "A1234BC",
        variantId = "variant-1",
        priceInPence = 250,
      ),
    )

    whenever(medusaProductService.findPriceList(prisonId))
      .thenReturn(existingPriceList)

    whenever(medusaProductService.deletePriceList(any())).thenReturn(ResponseEntity.noContent().build())

    // When
    service.recreatePriceList(prisonId, matchedProducts, regionId)

    // Then
    verify(medusaProductService).findPriceList(prisonId)
    verify(medusaProductService).deletePriceList("pl-123")

    argumentCaptor<CreatePriceListDto>().apply {
      verify(medusaProductService).createPriceList(
        capture(),
        eq(prisonId),
      )

      /*assertThat(firstValue.title).isEqualTo("MDI Price List")
      assertThat(firstValue.metadata.prisonId).isEqualTo(prisonId)
      assertThat(firstValue.prices).hasSize(1)

      assertThat(firstValue.prices[0].variantId)
        .isEqualTo("variant-1")
      assertThat(firstValue.prices[0].amount)
        .isEqualTo(250)
      assertThat(firstValue.prices[0].rules)
        .containsEntry("region_id", regionId)*/
    }
  }
}