package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldClientRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateClientTransactionRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateTransactionRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateTransactionResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.HOLD_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_NUMBER

class PrisonFinanceServiceTest {

  private val prisonFinanceClient: PrisonFinanceClient = mock()
  private val service = PrisonFinanceService(prisonFinanceClient)

  @Test
  fun `addHold builds request correctly and calls client`() {
    // Given
    val prisonId = PRISONER_ID
    val offenderNo = PRISONER_NUMBER
    val clientRequest = AddHoldClientRequest(amount = 10.5)

    val expectedResponse = AddHoldResponse(holdNumber = 123)
    whenever(prisonFinanceClient.addHold(any(), any(), any()))
      .thenReturn(expectedResponse)

    // When
    val result = service.addHold(prisonId, offenderNo, clientRequest)

    // Then
    val captor = argumentCaptor<AddHoldRequest>()

    verify(prisonFinanceClient).addHold(
      eq(prisonId),
      eq(offenderNo),
      captor.capture()
    )

    val request = captor.firstValue

    // Static fields
    assertEquals("HOLD", request.description)
    assertEquals(10.5, request.amount)
    assertEquals(offenderNo, request.clientName)

    // Generated fields
    assertTrue(request.clientTransactionId.length > "CLIENT-".length)
    assertTrue(request.clientUniqueReference.length > "CLIENT-".length)

    // Response passthrough
    assertEquals(expectedResponse, result)
  }

  @Test
  fun `releaseHold builds request correctly and calls client`() {
    // Given
    val prisonId = PRISONER_ID
    val offenderNo =PRISONER_NUMBER
    val holdNumber = HOLD_NUMBER

    val expectedResponse = ResponseEntity.noContent().build<Void>()
    whenever(prisonFinanceClient.releaseHold(any(), any(), any(), any()))
      .thenReturn(expectedResponse)

    // When
    val result = service.releaseHold(prisonId, offenderNo, holdNumber)

    // Then
    val captor = argumentCaptor<ReleaseHoldRequest>()

    verify(prisonFinanceClient).releaseHold(
      eq(prisonId),
      eq(offenderNo),
      eq(holdNumber),
      captor.capture()
    )

    val request = captor.firstValue

    // Static fields
    assertEquals("Remove HOLD", request.description)
    assertEquals(offenderNo, request.clientName)

    // Response passthrough
    assertEquals(expectedResponse, result)
  }

  @Test
  fun `releaseHoldCreateTransaction builds request correctly and calls client`() {
    // Given
    val prisonId = PRISONER_ID
    val offenderNo =PRISONER_NUMBER
    val holdNumber = HOLD_NUMBER
    val clientRequest = ReleaseHoldCreateClientTransactionRequest(transactionType = "PHONE")

    val expectedResponse = ReleaseHoldCreateTransactionResponse(id = "111-1")
    whenever(prisonFinanceClient.releaseHoleCreateTransaction(any(), any(), any(), any()))
      .thenReturn(expectedResponse)

    // When
    val result = service.releaseHoldAndCreateTransaction(prisonId, offenderNo, holdNumber, clientRequest)

    // Then
    val captor = argumentCaptor<ReleaseHoldCreateTransactionRequest>()

    verify(prisonFinanceClient).releaseHoleCreateTransaction(
      eq(prisonId),
      eq(offenderNo),
      eq(holdNumber),
      captor.capture()
    )

    val request = captor.firstValue

    // Static fields
    assertEquals("Remove HOLD", request.removeDescription)
    assertEquals("PHONE", request.type)
    assertEquals("HOLD for PHONE", request.createDescription)
    assertEquals(offenderNo, request.clientName)

    // Response passthrough
    assertEquals(expectedResponse, result)
  }
}
