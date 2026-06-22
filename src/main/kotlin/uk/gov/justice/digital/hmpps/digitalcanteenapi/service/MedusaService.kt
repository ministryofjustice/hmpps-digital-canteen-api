package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaAdminClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaStoreClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaDto

@Service
class MedusaService(
  private val medusaStoreClient: MedusaStoreClient,
  private val medusaAdminClient: MedusaAdminClient,
) {
  fun testMedusaEndpointStore(): Mono<MedusaDto> = medusaStoreClient.medusaStoreTest()
    .doOnError { e -> println("testMedusaServiceStore failed: ${e.message}") }

  fun testMedusaEndpointAdmin(): Mono<MedusaDto> = medusaAdminClient.medusaAdminTest()
    .doOnError { e -> println("testMedusaServiceAdmin failed: ${e.message}") }
}
