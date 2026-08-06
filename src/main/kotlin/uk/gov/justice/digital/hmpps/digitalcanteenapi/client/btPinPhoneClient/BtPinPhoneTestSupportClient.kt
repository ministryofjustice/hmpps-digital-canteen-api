package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtTokenRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtTokenResponse

@Component
class BtPinPhoneTestSupportClient(
  @Qualifier("btPinPhoneWebClient") private val btPinPhoneWebClient: WebClient,
  @Value("\${bt.client.id}") private val clientId: String,
  @Value("\${bt.client.secret}") private val clientSecret: String,
) {
  private fun getBtToken(): Mono<String> = btPinPhoneWebClient
    .post()
    .uri("/auth/token")
    .bodyValue(BtTokenRequest(clientId = clientId, clientSecret = clientSecret))
    .retrieve()
    .bodyToMono(BtTokenResponse::class.java)
    .map { it.accessToken }

  fun createAccount(request: CreateAccountRequest): Mono<CreateAccountResponse> = getBtToken().flatMap { token ->
    btPinPhoneWebClient
      .put()
      .uri("/PCS/Account")
      .headers { it.setBearerAuth(token) }
      .bodyValue(request)
      .retrieve()
      .bodyToMono(CreateAccountResponse::class.java)
  }

  fun createControlledNumber(request: CreateControlledNumberRequest): Mono<CreateControlledNumberResponse> = getBtToken().flatMap { token ->
    btPinPhoneWebClient
      .put()
      .uri("/pcs/ControlledNumber")
      .headers { it.setBearerAuth(token) }
      .bodyValue(request)
      .retrieve()
      .bodyToMono(CreateControlledNumberResponse::class.java)
  }

  fun getRelationships(): Mono<BtRelationshipsResponse> = getBtToken().flatMap { token ->
    btPinPhoneWebClient
      .post()
      .uri("/pcs/Relationships")
      .headers { it.setBearerAuth(token) }
      .bodyValue(emptyMap<String, Any>())
      .retrieve()
      .bodyToMono(BtRelationshipsResponse::class.java)
  }
}

data class CreateAccountRequest(
  val reference: String,
  val prisonerId: String,
  val prisonCode: String,
  val firstName: String,
  val middleName: String,
  val lastName: String,
  val statusActive: Boolean,
)

data class CreateControlledNumberRequest(
  val reference: String,
  val prisonerId: String,
  val name: String,
  val number: String,
  val relationshipId: Int,
  val controlStatus: Boolean,
  val alert: Boolean,
  val legal: Boolean,
  val override: Boolean,
  val allowMonitor: Boolean,
  val callAllowed: Boolean,
)

data class CreateAccountResponse(
  val reference: String,
  val prisonerId: String,
)

data class CreateControlledNumberResponse(
  val reference: String,
  val prisonerId: String,
  val id: Int,
)

data class BtRelationshipsResponse(
  val relationships: List<BtRelationship>,
)

data class BtRelationship(
  val id: Int,
  val description: String,
)
