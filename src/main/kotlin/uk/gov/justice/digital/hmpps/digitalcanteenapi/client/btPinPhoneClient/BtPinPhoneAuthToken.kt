package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtTokenRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtTokenResponse

@Component
class BtPinPhoneAuthToken(
  @Qualifier("btPinPhoneWebClient") private val btPinPhoneWebClient: WebClient,
  @Value("\${bt.client.id}") private val clientId: String,
  @Value("\${bt.client.secret}") private val clientSecret: String,
) {

  companion object {
    private val log = LoggerFactory.getLogger(BtPinPhoneAuthToken::class.java)
    private val objectMapper = jacksonObjectMapper()
  }

  @Synchronized
  fun getBtToken(): Mono<String> = btPinPhoneWebClient
    .post()
    .uri("/auth/token")
    .bodyValue(BtTokenRequest(clientId = clientId, clientSecret = clientSecret))
    .retrieve()
    .bodyToMono(String::class.java)
    .doOnNext { rawBody -> log.info("BT auth raw response: {}", rawBody) }
    .map { rawBody ->
      val response = objectMapper.readValue(rawBody, BtTokenResponse::class.java)
      response.accessToken
    }
}
