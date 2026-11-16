package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.AddHoldTransaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.ReleaseHoldAndCreateTransaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated.ReleaseHoldTransaction
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.HOLD_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.OFFENDER_BOOKING_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISON_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.PrisonApiMockServer
import java.math.BigDecimal

class PrisonFinanceClientTest {
  private lateinit var client: PrisonFinanceClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    val mapper = JsonMapper.builder()
      .findAndAddModules()
      .build()
    val webClientErrorHandler = WebClientErrorHandler(mapper)
    client = PrisonFinanceClient(webClient, webClientErrorHandler)
  }

  @Test
  fun `addHold - successfully adds hold`() {
    server.stubAddHold(PRISON_ID, PRISONER_NUMBER)
    val request = AddHoldTransaction(
      description = "HOLD",
      amount = 105,
      clientTransactionId = "trans1",
      clientName = PRISONER_NUMBER,
      clientUniqueReference = "ref1",
    )

    val result = client.addHold(PRISON_ID, PRISONER_NUMBER, request)

    assertThat(result).isNotNull
    assertThat(result.holdNumber).isEqualTo(12345)
  }

  @Test
  fun `addHold - throws UpstreamException on failure`() {
    server.stubAddHoldFailure(PRISON_ID, PRISONER_NUMBER, 400, "Insufficient funds")
    val request = AddHoldTransaction(
      description = "HOLD",
      amount = 10000,
      clientTransactionId = "trans1",
      clientName = PRISONER_NUMBER,
      clientUniqueReference = "ref1",
    )

    assertThatThrownBy {
      client.addHold(PRISON_ID, PRISONER_NUMBER, request)
    }.isInstanceOf(UpstreamException::class.java)
      .hasMessage("Insufficient funds")
  }

  @Test
  fun `releaseHold - successfully releases hold`() {
    server.stubReleaseHold(PRISON_ID, PRISONER_NUMBER, HOLD_NUMBER)
    val request = ReleaseHoldTransaction(
      description = "Remove HOLD",
      clientTransactionId = "trans2",
      clientName = PRISONER_NUMBER,
      clientUniqueReference = "ref2",
    )

    val result = client.releaseHold(PRISON_ID, PRISONER_NUMBER, HOLD_NUMBER, request)

    assertThat(result.statusCode.value()).isEqualTo(201)
  }

  @Test
  fun `releaseHold - throws UpstreamException on failure`() {
    server.stubReleaseHoldFailure(PRISON_ID, PRISONER_NUMBER, HOLD_NUMBER, 404, "Hold not found")
    val request = ReleaseHoldTransaction(
      description = "Remove HOLD",
      clientTransactionId = "trans2",
      clientName = PRISONER_NUMBER,
      clientUniqueReference = "ref2",
    )

    assertThatThrownBy {
      client.releaseHold(PRISON_ID, PRISONER_NUMBER, HOLD_NUMBER, request)
    }.isInstanceOf(UpstreamException::class.java)
      .hasMessage("Hold not found")
  }

  @Test
  fun `releaseHoleCreateTransaction - successfully releases hold and creates transaction`() {
    server.stubRelaseHoldAndCreateTransaction(PRISON_ID, PRISONER_NUMBER, HOLD_NUMBER)
    val request = ReleaseHoldAndCreateTransaction(
      type = ReleaseHoldAndCreateTransaction.Type.PHONE,
      removeDescription = "Remove HOLD",
      createDescription = "HOLD for PHONE",
      clientTransactionId = "trans3",
      clientName = PRISONER_NUMBER,
      removeClientUniqueReference = "ref-rem",
      createClientUniqueReference = "ref-cre",
    )

    val result = client.releaseHoldCreateTransaction(PRISON_ID, PRISONER_NUMBER, HOLD_NUMBER, request)

    assertThat(result).isNotNull
    assertThat(result.id).isEqualTo("468198331-1")
  }

  @Test
  fun `releaseHoleCreateTransaction - throws UpstreamException on failure`() {
    server.stubRelaseHoldAndCreateTransactionFailure(
      PRISON_ID,
      PRISONER_NUMBER,
      HOLD_NUMBER,
      500,
      "Internal Server Error",
    )
    val request = ReleaseHoldAndCreateTransaction(
      type = ReleaseHoldAndCreateTransaction.Type.PHONE,
      removeDescription = "Remove HOLD",
      createDescription = "HOLD for PHONE",
      clientTransactionId = "trans3",
      clientName = PRISONER_NUMBER,
      removeClientUniqueReference = "ref-rem",
      createClientUniqueReference = "ref-cre",
    )

    assertThatThrownBy {
      client.releaseHoldCreateTransaction(PRISON_ID, PRISONER_NUMBER, HOLD_NUMBER, request)
    }.isInstanceOf(UpstreamException::class.java)
      .hasMessage("Internal Server Error")
  }

  @Test
  fun `getPrisonerBalance - successfully retrieves prisoner balance info`() {
    server.stubGetPrisonerBalance(OFFENDER_BOOKING_ID)

    val result = client.getPrisonerBalance(OFFENDER_BOOKING_ID).block()

    assertThat(result).isNotNull
    assertThat(result?.spends).isEqualByComparingTo(BigDecimal("12.22"))
    assertThat(result?.cash).isEqualByComparingTo(BigDecimal("10.00"))
  }

  @Test
  fun `getPrisonerBalance - throws UpstreamException on failure`() {
    server.stubGetPrisonerBalanceTransactionFailure(
      OFFENDER_BOOKING_ID,
      500,
      "Internal Server Error",
    )

    assertThatThrownBy {
      client.getPrisonerBalance(OFFENDER_BOOKING_ID).block()
    }.isInstanceOf(UpstreamException::class.java)
      .hasMessage("Internal Server Error")
  }

  companion object {
    @JvmField
    internal val server = PrisonApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      server.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      server.stop()
    }
  }
}
