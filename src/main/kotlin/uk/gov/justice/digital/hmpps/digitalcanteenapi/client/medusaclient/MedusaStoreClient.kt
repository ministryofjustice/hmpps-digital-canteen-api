package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.bind.Bindable.mapOf
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.CompleteCartResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaCreateCartRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaCreateCartResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.PaymentResult

@Component
class MedusaStoreClient(
  @Qualifier("medusaStoreWebClient") private val medusaStoreClient: WebClient,
) {

  fun medusaStoreTest(): Mono<MedusaDto> = medusaStoreClient
    .get()
    .uri("/store/request-from-api")
    .retrieve()
    .bodyToMono(MedusaDto::class.java)

  fun completeCart(cartId: String, request: PaymentResult): CompleteCartResponse = medusaStoreClient
    .post()
    .uri("/store/pin-phone/carts/$cartId/complete")
    .bodyValue(mapOf<String, Any>("PaymentResult" to request))
    .retrieve()
    .bodyToMono(CompleteCartResponse::class.java)
    .block()!!

  fun createCart(cartRequest: MedusaCreateCartRequest): MedusaCreateCartResponse = medusaStoreClient
    .post()
    .uri ("/store/pin-phone/carts")
    .bodyValue(cartRequest)
    .retrieve()
    .bodyToMono(MedusaCreateCartResponse::class.java)
    .block()!!
}
