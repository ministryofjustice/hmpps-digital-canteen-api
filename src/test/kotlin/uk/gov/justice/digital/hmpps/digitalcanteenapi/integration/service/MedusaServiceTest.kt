package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaAdminClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.MedusaService

@ExtendWith(MockitoExtension::class)
class MedusaServiceTest {

  @Mock
  lateinit var medusaStoreClient: MedusaStoreClient

  @Mock
  lateinit var medusaAdminClient: MedusaAdminClient

  private lateinit var service: MedusaService

  @BeforeEach
  fun beforeEach() {
    service = MedusaService(medusaStoreClient, medusaAdminClient)
  }

  @Test
  fun `testMedusaEndpointStore - returns call from medusa store`() {
    whenever(medusaStoreClient.medusaStoreTest())
      .thenReturn(Mono.just(MedusaDto("success store call")))

    val result = service.testMedusaEndpointStore()

    StepVerifier.create(result)
      .assertNext { response ->
        assertThat(response.status).isEqualTo("success store call")
      }
      .verifyComplete()
  }

  @Test
  fun `testMedusaEndpointAdmin - returns call from medusa admin`() {
    whenever(medusaAdminClient.medusaAdminTest())
      .thenReturn(Mono.just(MedusaDto("success admin call")))

    val result = service.testMedusaEndpointAdmin()

    StepVerifier.create(result)
      .assertNext { response ->
        assertThat(response.status).isEqualTo("success admin call")
      }
      .verifyComplete()
  }
}
