package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerenrichment.dto.PrisonerSearchDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentResult
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentStatus
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateTransactionResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.CART_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.HOLD_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_NUMBER

class PinPhoneBuyCreditOrchestrationServiceTest {

  private val financeService: PrisonFinanceService = mock()
  private val prisonerEnrichmentService: PrisonerEnrichmentService = mock()
  private val medusaStoreClient: MedusaStoreClient = mock()

  private lateinit var service: PinPhoneBuyCreditOrchestrationService

  @BeforeEach
  fun setUp() {
    service = PinPhoneBuyCreditOrchestrationService(
      financeService,
      prisonerEnrichmentService,
      medusaStoreClient
    )
  }

  @Test
  fun `processCheckout successfully processes payment`() {
    // Given
    val cartId = CART_ID
    val amount = 10.0
    val prisonerSearchDto = PrisonerSearchDto(
      prisonerNumber = PRISONER_NUMBER,
      prisonId = PRISONER_ID
    )
    val enrichedPrisoner = PrisonerEnrichmentService.EnrichedPrisonerDto(
      prisoner = prisonerSearchDto,
      foodAllergies = null,
      medicalDietaryRequirements = null,
      personalisedDietaryRequirements = null,
      cateringInstructions = null,
      incentives = null,
      hasActiveAdjudications = false,
      activeAdjudications = null
    )

    whenever(prisonerEnrichmentService.getEnrichedPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(enrichedPrisoner))

    whenever(financeService.addHold(any(), any(), any()))
      .thenReturn(AddHoldResponse(holdNumber = HOLD_NUMBER))

    whenever(financeService.releaseHoldAndCreateTransaction(any(), any(), any(), any()))
      .thenReturn(ReleaseHoldCreateTransactionResponse(id = "tx123"))

    whenever(medusaStoreClient.completeCart(any(), any()))
      .thenReturn(Mono.just(MedusaDto(status = "completed")))

    // When
    val result = service.processCheckout(PRISONER_NUMBER, amount, cartId)

    // Then
    assertEquals(PaymentStatus.AUTHORIZED.toString(), result)

    verify(prisonerEnrichmentService).getEnrichedPrisoner(PRISONER_NUMBER)
    verify(financeService).addHold(eq(PRISONER_ID), eq(PRISONER_NUMBER), any())
    verify(financeService).releaseHoldAndCreateTransaction(eq(PRISONER_ID), eq(PRISONER_NUMBER), eq(HOLD_NUMBER), any())
    
    val paymentResultCaptor = argumentCaptor<PaymentResult>()
    verify(medusaStoreClient).completeCart(any(), paymentResultCaptor.capture())
    assertEquals(PaymentStatus.AUTHORIZED, paymentResultCaptor.firstValue.status)
  }

  @Test
  fun `processCheckout returns error and releases hold when transaction fails`() {
    // Given
    val cartId = CART_ID
    val amount = 10.0
    val prisonerSearchDto = PrisonerSearchDto(
      prisonerNumber = PRISONER_NUMBER,
      prisonId = PRISONER_ID
    )
    val enrichedPrisoner = PrisonerEnrichmentService.EnrichedPrisonerDto(
      prisoner = prisonerSearchDto,
      foodAllergies = null,
      medicalDietaryRequirements = null,
      personalisedDietaryRequirements = null,
      cateringInstructions = null,
      incentives = null,
      hasActiveAdjudications = false,
      activeAdjudications = null
    )

    whenever(prisonerEnrichmentService.getEnrichedPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(enrichedPrisoner))

    whenever(financeService.addHold(any(), any(), any()))
      .thenReturn(AddHoldResponse(holdNumber = HOLD_NUMBER))

    whenever(financeService.releaseHoldAndCreateTransaction(any(), any(), any(), any()))
      .thenThrow(RuntimeException("Transaction failed"))

    whenever(medusaStoreClient.completeCart(any(), any()))
      .thenReturn(Mono.just(MedusaDto(status = "error")))

    // When
    val result = service.processCheckout(PRISONER_NUMBER, amount, cartId)

    // Then
    assertEquals(PaymentStatus.ERROR.toString(), result)
    verify(financeService).releaseHold(eq(PRISONER_ID), eq(PRISONER_NUMBER), eq(HOLD_NUMBER))
    
    val paymentResultCaptor = argumentCaptor<PaymentResult>()
    verify(medusaStoreClient).completeCart(any(), paymentResultCaptor.capture())
    assertEquals(PaymentStatus.ERROR, paymentResultCaptor.firstValue.status)
  }

  @Test
  fun `processCheckout returns error and releases hold when upstream error occurs`() {
    // Given
    val prisonerSearchDto = PrisonerSearchDto(
      prisonerNumber = PRISONER_NUMBER,
      prisonId = PRISONER_ID,
    )
    val enrichedPrisoner = PrisonerEnrichmentService.EnrichedPrisonerDto(
      prisoner = prisonerSearchDto,
      foodAllergies = null,
      medicalDietaryRequirements = null,
      personalisedDietaryRequirements = null,
      cateringInstructions = null,
      incentives = null,
      hasActiveAdjudications = false,
      activeAdjudications = null,
    )

    whenever(prisonerEnrichmentService.getEnrichedPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(enrichedPrisoner))

    whenever(financeService.addHold(any(), any(), any()))
      .thenReturn(AddHoldResponse(holdNumber = HOLD_NUMBER))

    whenever(financeService.releaseHoldAndCreateTransaction(any(), any(), any(), any()))
      .thenThrow(UpstreamException("Upstream error"))

    whenever(medusaStoreClient.completeCart(any(), any()))
      .thenReturn(Mono.just(MedusaDto(status = "error")))

    // When
    val result = service.processCheckout(PRISONER_NUMBER, 10.0, CART_ID)

    // Then
    assertEquals(PaymentStatus.ERROR.toString(), result)
    verify(financeService).releaseHold(eq(PRISONER_ID), eq(PRISONER_NUMBER), eq(HOLD_NUMBER))

    val paymentResultCaptor = argumentCaptor<PaymentResult>()
    verify(medusaStoreClient).completeCart(any(), paymentResultCaptor.capture())
    assertEquals(PaymentStatus.ERROR, paymentResultCaptor.firstValue.status)
    assertEquals("Upstream error", paymentResultCaptor.firstValue.errorMessage)
  }

  @Test
  fun `processCheckout throws exception when prisoner not found`() {
    // Given
    whenever(prisonerEnrichmentService.getEnrichedPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.empty())

    // When / Then
    try {
      service.processCheckout(PRISONER_NUMBER, 10.0, CART_ID)
    } catch (e: ResponseStatusException) {
      assertEquals(HttpStatus.NOT_FOUND, e.statusCode)
    }
  }
}
