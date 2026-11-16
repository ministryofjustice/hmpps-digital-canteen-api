package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.PRISON_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.existingPriceList
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.matchedProducts
import uk.gov.justice.digital.hmpps.digitalcanteenapi.fixtures.TestMockData.region
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.CreatePriceListDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.PriceListPrice
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.PriceListService

@ExtendWith(MockitoExtension::class)
class PriceListServiceTest {

  @Mock
  private lateinit var medusaProductService: MedusaProductService

  @InjectMocks
  private lateinit var service: PriceListService

  @BeforeEach
  fun setup() {
    service = PriceListService(medusaProductService)
  }

  @Test
  fun `should delete existing price list and create new one`() {
    // Given
    whenever(medusaProductService.findPriceList(PRISON_ID))
      .thenReturn(existingPriceList)

    // When
    service.recreatePriceList(
      prisonId = PRISON_ID,
      matchedProducts = matchedProducts,
      regionId = region.id,
    )

    // Then
    verify(medusaProductService).findPriceList(PRISON_ID)
    verify(medusaProductService).deletePriceList("price-list-id")

    argumentCaptor<CreatePriceListDto>().apply {
      verify(medusaProductService).createPriceList(capture(), eq(PRISON_ID))

      val dto = firstValue

      assertThat(dto.title).isEqualTo("MDI Price List")
      assertThat(dto.description).isEqualTo("MDI Price List")
      assertThat(dto.status).isEqualTo("active")
      assertThat(dto.metadata?.prisonId).isEqualTo(PRISON_ID)

      assertThat(dto.prices).hasSize(2)

      assertThat(dto.prices[0]).isEqualTo(
        PriceListPrice(
          variantId = "variant-1",
          amount = 100,
          currencyCode = "gbp",
          rules = mapOf("region_id" to region.id),
        ),
      )

      assertThat(dto.prices[1]).isEqualTo(
        PriceListPrice(
          variantId = "variant-2",
          amount = 200,
          currencyCode = "gbp",
          rules = mapOf("region_id" to region.id),
        ),
      )
    }
  }

  @Test
  fun `should create price list when existing one does not exist`() {
    // Given
    whenever(medusaProductService.findPriceList(PRISON_ID))
      .thenReturn(null)

    // When
    service.recreatePriceList(PRISON_ID, matchedProducts, region.id)

    // Then
    verify(medusaProductService, never()).deletePriceList(any())
    verify(medusaProductService).createPriceList(any(), eq(PRISON_ID))
  }
}
