package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.MedusaMockServer

class MedusaStoreClientTest {
  private lateinit var client: MedusaStoreClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    client = MedusaStoreClient(webClient)
  }

  @Test
  fun `getMedusaStoreTest- successfully returns store route request`() {
    server.stubGetMedusaStoreTest()

    val result = client.medusaStoreTest().block()

    assertThat(result).isNotNull
    with(result!!) {
      assertThat(status).isEqualTo("successful call to medusa store")
    }
  }

  companion object {
    @JvmField
    internal val server = MedusaMockServer()

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
