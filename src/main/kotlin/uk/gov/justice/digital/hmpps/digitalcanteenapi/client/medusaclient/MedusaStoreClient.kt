package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaDto

@Component
class MedusaStoreClient(
  @Qualifier("medusaStoreWebClient") private val medusaStoreClient: WebClient,
) {

  fun medusaStoreTest(): Mono<MedusaDto> = medusaStoreClient
    .get()
    .uri("/store/test-request-from-api")
    .retrieve()
    .bodyToMono(MedusaDto::class.java)
}
