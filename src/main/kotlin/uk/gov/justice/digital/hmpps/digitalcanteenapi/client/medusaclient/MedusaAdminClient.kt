package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaDto

@Component
class MedusaAdminClient(
  @Qualifier("medusaAdminWebClient") private val webClient: WebClient,
  @param:Value("\${admin-email}") private val adminEmail: String,
  @param:Value("\${admin-password}") private val adminPassword: String,
) {

  private fun getAdminToken(): Mono<String> = webClient
    .post()
    .uri("/auth/user/emailpass")
    .bodyValue(mapOf("email" to adminEmail, "password" to adminPassword))
    .retrieve()
    .bodyToMono(Map::class.java)
    .map { it["token"] as String }

  fun medusaAdminTest(): Mono<MedusaDto> = getAdminToken()
    .flatMap { token ->
      webClient
        .get()
        .uri("/admin/test-request-from-api")
        .header("Authorization", "Bearer $token")
        .retrieve()
        .bodyToMono(MedusaDto::class.java)
    }
}
