package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneBalanceRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneBalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneBuyCreditRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneContactsRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneContactsResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtTokenRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtTokenResponse

@Component
class BtPinPhoneClient(
  @Qualifier("btPinPhoneWebClient") private val btPinPhoneWebClient: WebClient,
  @Value("\${bt.client.id}") private val clientId: String,
  @Value("\${bt.client.secret}") private val clientSecret: String,
) {

  fun getBtToken(): Mono<BtTokenResponse> = btPinPhoneWebClient
    .post()
    .uri("/auth/token")
    .bodyValue(BtTokenRequest(clientId = clientId, clientSecret = clientSecret))
    .retrieve()
    .bodyToMono(BtTokenResponse::class.java)

  fun getPrisonerBalance(btPinPhoneBalanceRequest: BtPinPhoneBalanceRequest): Mono<BtPinPhoneBalanceResponseDto> = getBtToken().flatMap { btAuthResponse ->
    btPinPhoneWebClient
      .post()
      .uri("/pcs/Balance")
      .headers { it.setBearerAuth(btAuthResponse.accessToken) }
      .bodyValue(btPinPhoneBalanceRequest)
      .retrieve()
      .bodyToMono(BtPinPhoneBalanceResponseDto::class.java)
  }

  fun getPrisonerContacts(btPinPhoneContactRequest: BtPinPhoneContactsRequest): Mono<BtPinPhoneContactsResponseDto> = getBtToken().flatMap { btAuthResponse ->
    btPinPhoneWebClient
      .post()
      .uri("/pcs/ControlledNumbers")
      .headers { it.setBearerAuth(btAuthResponse.accessToken) }
      .bodyValue(btPinPhoneContactRequest)
      .retrieve()
      .bodyToMono(BtPinPhoneContactsResponseDto::class.java)
  }

  @Suppress("UnusedParameter")
  fun addCredit(btPinPhoneBuyCreditRequest: BtPinPhoneBuyCreditRequest): Mono<Void> = Mono.empty()
}
