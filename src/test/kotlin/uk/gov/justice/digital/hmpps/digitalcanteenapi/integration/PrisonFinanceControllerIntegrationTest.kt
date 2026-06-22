package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldClientRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateClientTransactionRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.PrisonApiExtension.Companion.prisonApi

class PrisonFinanceControllerIntegrationTest : IntegrationTestBase() {

  @Test
  fun `addHold - successfully adds a hold`() {
    val request = AddHoldClientRequest(
      amount = 1634,
    )

    hmppsAuth.stubGrantToken()
    prisonApi.stubAddHold(PRISONER_ID, OFFENDER_ID)

    webTestClient.post()
      .uri("/api/finance/prisons/{prisonId}/offenders/{offenderNo}/addHold", PRISONER_ID, OFFENDER_ID)
      .headers(setAuthorisation())
      .bodyValue(request)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.holdNumber").isEqualTo(12345)
  }

  @Test
  fun `addHold - returns 400 when insufficient balance`() {
    val request = AddHoldClientRequest(
      amount = 1000000,
    )
    val errorMessage = "Insufficient funds"

    hmppsAuth.stubGrantToken()
    prisonApi.stubAddHoldFailure(PRISONER_ID, OFFENDER_ID, 400, errorMessage)

    webTestClient.post()
      .uri("/api/finance/prisons/{prisonId}/offenders/{offenderNo}/addHold", PRISONER_ID, OFFENDER_ID)
      .headers(setAuthorisation())
      .bodyValue(request)
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.message").isEqualTo(errorMessage)
  }

  @Test
  fun `releaseHold - successfully releases a hold`() {
    hmppsAuth.stubGrantToken()
    prisonApi.stubReleaseHold(PRISONER_ID, OFFENDER_ID, HOLD_NUMBER)

    webTestClient.post()
      .uri("/api/finance/prisons/{prisonId}/offenders/{offenderNo}/releaseHold/{holdNumber}", PRISONER_ID, OFFENDER_ID, HOLD_NUMBER)
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isCreated
      .expectBody().isEmpty
  }

  @Test
  fun `releaseHold - returns 400 when release fails`() {
    val errorMessage = "Hold not found"
    hmppsAuth.stubGrantToken()
    prisonApi.stubReleaseHoldFailure(PRISONER_ID, OFFENDER_ID, HOLD_NUMBER, 400, errorMessage)

    webTestClient.post()
      .uri("/api/finance/prisons/{prisonId}/offenders/{offenderNo}/releaseHold/{holdNumber}", PRISONER_ID, OFFENDER_ID, HOLD_NUMBER)
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.message").isEqualTo(errorMessage)
  }

  @Test
  fun `releaseHoldAndCreateTransaction - successfully releases a hold and creates a transaction`() {
    val request = ReleaseHoldCreateClientTransactionRequest(transactionType = "PHONE")
    hmppsAuth.stubGrantToken()
    prisonApi.stubRelaseHoldAndCreateTransaction(PRISONER_ID, OFFENDER_ID, HOLD_NUMBER)

    webTestClient.post()
      .uri("/api/finance/prisons/{prisonId}/offenders/{offenderNo}/releaseHoldCreateTransaction/{holdNumber}", PRISONER_ID, OFFENDER_ID, HOLD_NUMBER)
      .headers(setAuthorisation())
      .bodyValue(request)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.id").isEqualTo("468198331-1")
  }

  @Test
  fun `releaseHoldAndCreateTransaction - returns 400 when request fails`() {
    val request = ReleaseHoldCreateClientTransactionRequest(transactionType = "PHONE")
    val errorMessage = "Transaction creation failed"
    hmppsAuth.stubGrantToken()
    prisonApi.stubRelaseHoldAndCreateTransactionFailure(PRISONER_ID, OFFENDER_ID, HOLD_NUMBER, 400, errorMessage)

    webTestClient.post()
      .uri("/api/finance/prisons/{prisonId}/offenders/{offenderNo}/releaseHoldCreateTransaction/{holdNumber}", PRISONER_ID, OFFENDER_ID, HOLD_NUMBER)
      .headers(setAuthorisation())
      .bodyValue(request)
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.message").isEqualTo(errorMessage)
  }
}
