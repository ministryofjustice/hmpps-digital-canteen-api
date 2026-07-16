package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.Order
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentResult
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentStatus
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateTransactionResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.CART_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.HOLD_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment.PinPhonePrisonerEnrichmentService
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment.dto.PrisonerSearchResponseDto
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.CompleteCartResponse as MedusaCompleteCartResponse

class PinPhoneBuyCreditOrchestrationServiceTest {

  private val financeService: PrisonFinanceService = mock()
  private val pinPhonePrisonerEnrichmentService: PinPhonePrisonerEnrichmentService = mock()
  private val medusaStoreClient: MedusaStoreClient = mock()
  private val btPinPhoneClient: BtPinPhoneClient = mock()

  private lateinit var service: PinPhoneBuyCreditOrchestrationService

  @BeforeEach
  fun setUp() {
    service = PinPhoneBuyCreditOrchestrationService(
      financeService,
      pinPhonePrisonerEnrichmentService,
      medusaStoreClient,
      btPinPhoneClient,
    )
  }

  @Test
  fun `processCheckout successfully processes payment`() {
    // Given
    val cartId = CART_ID
    val amount = 10.0
    val prisonerSearchResponseDto = PrisonerSearchResponseDto(
      prisonerNumber = PRISONER_NUMBER,
      prisonId = PRISONER_ID,
      prisonName = "HMP Wandsworth",
      bookNumber = "B1234",
      bookingId = "12345",
      dateOfBirth = LocalDate.of(1990, 1, 1),
      youthOffender = false,
      gender = "Male",
    )
    val enrichedPrisoner = PinPhonePrisonerEnrichmentService.EnrichedPinPhonePrisonerDto(
      prisoner = prisonerSearchResponseDto,
      incentives = mock(),
      prisonerBalance = null,
      prisonerBtBalance = null,
      hasActiveAdjudications = false,
      activeAdjudications = null,
    )

    whenever(pinPhonePrisonerEnrichmentService.getEnrichedPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(enrichedPrisoner))

    whenever(financeService.addHold(any(), any(), any()))
      .thenReturn(AddHoldResponse(holdNumber = HOLD_NUMBER))

    whenever(btPinPhoneClient.addCredit(any()))
      .thenReturn(Mono.empty())

    whenever(financeService.releaseHoldAndCreateTransaction(any(), any(), any(), any()))
      .thenReturn(ReleaseHoldCreateTransactionResponse(id = "tx123"))

    whenever(medusaStoreClient.completeCart(any(), any()))
      .thenReturn(MedusaCompleteCartResponse(order = Order(id = "order123")))

    // When
    val result = service.processCheckout(PRISONER_NUMBER, amount, cartId)

    // Then
    assertEquals("AUTHORIZED", result.status)
    assertEquals("order123", result.orderId)
    assertEquals("Cart completed successfully", result.message)

    verify(pinPhonePrisonerEnrichmentService).getEnrichedPrisoner(PRISONER_NUMBER)
    verify(financeService).addHold(eq(PRISONER_ID), eq(PRISONER_NUMBER), any())
    verify(btPinPhoneClient).addCredit(any())
    verify(financeService).releaseHoldAndCreateTransaction(eq(PRISONER_ID), eq(PRISONER_NUMBER), eq(HOLD_NUMBER), any())

    val paymentResultCaptor = argumentCaptor<PaymentResult>()
    verify(medusaStoreClient).completeCart(eq(cartId), paymentResultCaptor.capture())
    assertEquals(PaymentStatus.AUTHORIZED, paymentResultCaptor.firstValue.status)
  }

  @Test
  fun `processCheckout retries BT API and succeeds`() {
    // Given
    val cartId = CART_ID
    val amount = 10.0
    val enrichedPrisoner = createEnrichedPrisoner()

    whenever(pinPhonePrisonerEnrichmentService.getEnrichedPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(enrichedPrisoner))

    whenever(financeService.addHold(any(), any(), any()))
      .thenReturn(AddHoldResponse(holdNumber = HOLD_NUMBER))

    whenever(btPinPhoneClient.addCredit(any()))
      .thenReturn(Mono.error(RuntimeException("BT failed")))
      .thenReturn(Mono.empty())

    whenever(financeService.releaseHoldAndCreateTransaction(any(), any(), any(), any()))
      .thenReturn(ReleaseHoldCreateTransactionResponse(id = "tx123"))

    whenever(medusaStoreClient.completeCart(any(), any()))
      .thenReturn(MedusaCompleteCartResponse(order = Order(id = "order123")))

    // When
    val result = service.processCheckout(PRISONER_NUMBER, amount, cartId)

    // Then
    assertEquals("AUTHORIZED", result.status)
    assertEquals("order123", result.orderId)
    assertEquals("Cart completed successfully", result.message)
    verify(btPinPhoneClient, times(2)).addCredit(any())
  }

  @Test
  fun `processCheckout retries BT API and fails`() {
    // Given
    val cartId = CART_ID
    val amount = 10.0
    val enrichedPrisoner = createEnrichedPrisoner()
    whenever(pinPhonePrisonerEnrichmentService.getEnrichedPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(enrichedPrisoner))
    whenever { financeService.addHold(any(), any(), any()) }.thenReturn(AddHoldResponse(holdNumber = HOLD_NUMBER))
    whenever(btPinPhoneClient.addCredit(any()))
      .thenReturn(Mono.error(RuntimeException("BT failed")))
      .thenReturn(Mono.error(RuntimeException("BT failed again")))
      .thenReturn(Mono.error(RuntimeException("BT Down")))
    whenever(financeService.releaseHoldAndCreateTransaction(any(), any(), any(), any()))
      .thenReturn(ReleaseHoldCreateTransactionResponse(id = "tx123"))
    whenever(medusaStoreClient.completeCart(any(), any()))
      .thenReturn(MedusaCompleteCartResponse(code = "ORDER_NOT_FOUND", message = "Order not found"))
    val result = service.processCheckout(PRISONER_NUMBER, amount, cartId)

    // Then
    assertEquals("ERROR", result.status)
    assertEquals(null, result.orderId)
    assertEquals("BT Down", result.message)
    verify(btPinPhoneClient, times(3)).addCredit(any())

  }

  @Test
  fun `processCheckout returns error and releases hold when transaction fails`() {
    // Given
    val cartId = CART_ID
    val amount = 10.0
    val enrichedPrisoner = createEnrichedPrisoner()

    whenever(pinPhonePrisonerEnrichmentService.getEnrichedPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(enrichedPrisoner))

    whenever(financeService.addHold(any(), any(), any()))
      .thenReturn(AddHoldResponse(holdNumber = HOLD_NUMBER))

    whenever(btPinPhoneClient.addCredit(any()))
      .thenReturn(Mono.empty())

    whenever(financeService.releaseHoldAndCreateTransaction(any(), any(), any(), any()))
      .thenThrow(RuntimeException("Transaction failed"))

    // When
    val result = service.processCheckout(PRISONER_NUMBER, amount, cartId)

    // Then
    assertEquals("ERROR", result.status)
    assertEquals("Transaction failed", result.message)
    verify(financeService).releaseHold(eq(PRISONER_ID), eq(PRISONER_NUMBER), eq(HOLD_NUMBER))

    val paymentResultCaptor = argumentCaptor<PaymentResult>()
    verify(medusaStoreClient).completeCart(eq(cartId), paymentResultCaptor.capture())
    assertEquals(PaymentStatus.ERROR, paymentResultCaptor.firstValue.status)
  }

  @Test
  fun `processCheckout returns error and releases hold when upstream error occurs`() {
    // Given
    val enrichedPrisoner = createEnrichedPrisoner()

    whenever(pinPhonePrisonerEnrichmentService.getEnrichedPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.just(enrichedPrisoner))

    whenever(financeService.addHold(any(), any(), any()))
      .thenReturn(AddHoldResponse(holdNumber = HOLD_NUMBER))

    whenever(btPinPhoneClient.addCredit(any()))
      .thenReturn(Mono.empty())

    whenever(financeService.releaseHoldAndCreateTransaction(any(), any(), any(), any()))
      .thenThrow(UpstreamException("Upstream error"))

    // When
    val result = service.processCheckout(PRISONER_NUMBER, 10.0, CART_ID)

    // Then
    assertEquals("ERROR", result.status)
    assertEquals("Upstream error", result.message)
    verify(financeService).releaseHold(eq(PRISONER_ID), eq(PRISONER_NUMBER), eq(HOLD_NUMBER))

    val paymentResultCaptor = argumentCaptor<PaymentResult>()
    verify(medusaStoreClient).completeCart(eq(CART_ID), paymentResultCaptor.capture())
    assertEquals(PaymentStatus.ERROR, paymentResultCaptor.firstValue.status)
    assertEquals("Upstream error", paymentResultCaptor.firstValue.errorMessage)
  }

  @Test
  fun `processCheckout throws exception when prisoner not found`() {
    // Given
    whenever(pinPhonePrisonerEnrichmentService.getEnrichedPrisoner(PRISONER_NUMBER))
      .thenReturn(Mono.empty())

    // When / Then
    try {
      service.processCheckout(PRISONER_NUMBER, 10.0, CART_ID)
    } catch (e: ResponseStatusException) {
      assertEquals(HttpStatus.NOT_FOUND, e.statusCode)
    }
  }

  private fun createEnrichedPrisoner() = PinPhonePrisonerEnrichmentService.EnrichedPinPhonePrisonerDto(
    prisoner = PrisonerSearchResponseDto(
      prisonerNumber = PRISONER_NUMBER,
      prisonId = PRISONER_ID,
      prisonName = "HMP Wandsworth",
      bookNumber = "B1234",
      bookingId = "12345",
      dateOfBirth = LocalDate.of(1990, 1, 1),
      youthOffender = false,
      gender = "Male",
    ),
    incentives = mock(),
    prisonerBalance = null,
    prisonerBtBalance = null,
    hasActiveAdjudications = false,
    activeAdjudications = null,
  )
}
