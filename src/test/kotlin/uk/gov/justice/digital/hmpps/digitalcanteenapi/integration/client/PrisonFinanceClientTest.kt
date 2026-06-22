package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.AddHoldRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldCreateTransactionRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.ReleaseHoldRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.HOLD_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_ID
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.PrisonApiMockServer

class PrisonFinanceClientTest {
  private lateinit var client: PrisonFinanceClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    val mapper = JsonMapper.builder()
      .findAndAddModules()
      .build()
    client = PrisonFinanceClient(webClient, mapper)
  }

  @Test
  fun `addHold - successfully adds hold`() {
    server.stubAddHold(PRISONER_ID, PRISONER_NUMBER)
    val request = AddHoldRequest(
      description = "HOLD",
      amount = 10.5,
      clientTransactionId = "trans1",
      clientName = PRISONER_NUMBER,
      clientUniqueReference = "ref1"
    )

    val result = client.addHold(PRISONER_ID, PRISONER_NUMBER, request)

    assertThat(result).isNotNull
    assertThat(result.holdNumber).isEqualTo(12345)
  }

  @Test
  fun `addHold - throws UpstreamException on failure`() {
    server.stubAddHoldFailure(PRISONER_ID, PRISONER_NUMBER, 400, "Insufficient funds")
    val request = AddHoldRequest(
      description = "HOLD",
      amount = 1000.0,
      clientTransactionId = "trans1",
      clientName = PRISONER_NUMBER,
      clientUniqueReference = "ref1"
    )

    assertThatThrownBy {
      client.addHold(PRISONER_ID, PRISONER_NUMBER, request)
    }.isInstanceOf(UpstreamException::class.java)
      .hasMessage("Insufficient funds")
  }

  @Test
  fun `releaseHold - successfully releases hold`() {
    server.stubReleaseHold(PRISONER_ID, PRISONER_NUMBER, HOLD_NUMBER)
    val request = ReleaseHoldRequest(
      description = "Remove HOLD",
      clientTransactionId = "trans2",
      clientName = PRISONER_NUMBER,
      clientUniqueReference = "ref2"
    )

    val result = client.releaseHold(PRISONER_ID, PRISONER_NUMBER, HOLD_NUMBER, request)

    assertThat(result.statusCode.value()).isEqualTo(201)
  }

  @Test
  fun `releaseHold - throws UpstreamException on failure`() {
    server.stubReleaseHoldFailure(PRISONER_ID, PRISONER_NUMBER, HOLD_NUMBER, 404, "Hold not found")
    val request = ReleaseHoldRequest(
      description = "Remove HOLD",
      clientTransactionId = "trans2",
      clientName = PRISONER_NUMBER,
      clientUniqueReference = "ref2"
    )

    assertThatThrownBy {
      client.releaseHold(PRISONER_ID, PRISONER_NUMBER, HOLD_NUMBER, request)
    }.isInstanceOf(UpstreamException::class.java)
      .hasMessage("Hold not found")
  }

  @Test
  fun `releaseHoleCreateTransaction - successfully releases hold and creates transaction`() {
    server.stubRelaseHoldAndCreateTransaction(PRISONER_ID, PRISONER_NUMBER, HOLD_NUMBER)
    val request = ReleaseHoldCreateTransactionRequest(
      type = "PHONE",
      removeDescription = "Remove HOLD",
      createDescription = "HOLD for PHONE",
      clientTransactionId = "trans3",
      clientName = PRISONER_NUMBER,
      removeClientUniqueReference = "ref-rem",
      createClientUniqueReference = "ref-cre"
    )

    val result = client.releaseHoleCreateTransaction(PRISONER_ID, PRISONER_NUMBER, HOLD_NUMBER, request)

    assertThat(result).isNotNull
    assertThat(result.id).isEqualTo("468198331-1")
  }

  @Test
  fun `releaseHoleCreateTransaction - throws UpstreamException on failure`() {
    server.stubRelaseHoldAndCreateTransactionFailure(PRISONER_ID, PRISONER_NUMBER, HOLD_NUMBER, 500, "Internal Server Error")
    val request = ReleaseHoldCreateTransactionRequest(
      type = "PHONE",
      removeDescription = "Remove HOLD",
      createDescription = "HOLD for PHONE",
      clientTransactionId = "trans3",
      clientName = PRISONER_NUMBER,
      removeClientUniqueReference = "ref-rem",
      createClientUniqueReference = "ref-cre"
    )

    assertThatThrownBy {
      client.releaseHoleCreateTransaction(PRISONER_ID, PRISONER_NUMBER, HOLD_NUMBER, request)
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
