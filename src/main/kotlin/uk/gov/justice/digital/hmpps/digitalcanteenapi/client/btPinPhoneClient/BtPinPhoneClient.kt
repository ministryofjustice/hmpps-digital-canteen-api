package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBuyCreditRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneControlledNumbersRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneControlledNumbersResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtTokenRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtTokenResponse

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
    .bodyToMono<BtTokenResponse>()

  fun getPrisonerBalance(btPinPhoneBalanceRequest: BtPinPhoneBalanceRequest): Mono<BtPinPhoneBalanceResponse> = getBtToken().flatMap { btAuthResponse ->
    btPinPhoneWebClient
      .post()
      .uri("/pcs/Balance")
      .headers { it.setBearerAuth(btAuthResponse.accessToken) }
      .bodyValue(btPinPhoneBalanceRequest)
      .retrieve()
      .bodyToMono<BtPinPhoneBalanceResponse>()
  }

  fun getPrisonerContacts(btPinPhoneControlledNumbersRequest: BtPinPhoneControlledNumbersRequest): Mono<BtPinPhoneControlledNumbersResponse> = getBtToken().flatMap { btAuthResponse ->
    btPinPhoneWebClient
      .post()
      .uri("/pcs/ControlledNumbers")
      .headers { it.setBearerAuth(btAuthResponse.accessToken) }
      .bodyValue(btPinPhoneControlledNumbersRequest)
      .retrieve()
      .bodyToMono<BtPinPhoneControlledNumbersResponse>()
  }

  @Suppress("UnusedParameter")
  fun addCredit(btPinPhoneBuyCreditRequest: BtPinPhoneBuyCreditRequest): Mono<Void> = Mono.empty()
}
