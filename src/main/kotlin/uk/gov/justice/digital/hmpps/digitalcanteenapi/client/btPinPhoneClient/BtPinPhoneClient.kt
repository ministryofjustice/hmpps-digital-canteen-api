package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneBalanceRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneBalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneBuyCreditRequest

@Component
class BtPinPhoneClient(
  @Qualifier("btPinPhoneWebClient") private val btPinPhoneWebClient: WebClient,
  private val btPinPhoneAuthToken: BtPinPhoneAuthToken,
) {

  fun getPrisonerBalance(prisonerNumber: String): Mono<BtPinPhoneBalanceResponseDto> = Mono.just(
    BtPinPhoneBalanceResponseDto(
      reference = "reference_FN",
      prisonerId = prisonerNumber,
      balancePence = 1220,
      creditLimitPence = 300,
    ),
  )

  fun getPrisonerBalanceUpdated(btPinPhoneBalanceRequest: BtPinPhoneBalanceRequest): Mono<BtPinPhoneBalanceResponseDto> = btPinPhoneAuthToken.getBtToken().flatMap { token ->
    btPinPhoneWebClient
      .post()
      .uri("/pcs/Balance")
      .headers { it.setBearerAuth(token) }
      .bodyValue(btPinPhoneBalanceRequest)
      .retrieve()
      .bodyToMono(BtPinPhoneBalanceResponseDto::class.java)
  }

  fun getPrisonerBalanceUpdated2(btPinPhoneBalanceRequest: BtPinPhoneBalanceRequest): Mono<BtPinPhoneBalanceResponseDto> = btPinPhoneAuthToken.getBtToken().flatMap { token ->
    btPinPhoneWebClient
      .post()
      .uri("/pcs/balance")
      .headers { it.setBearerAuth(token) }
      .bodyValue(btPinPhoneBalanceRequest)
      .retrieve()
      .bodyToMono(BtPinPhoneBalanceResponseDto::class.java)
  }

  @Suppress("UnusedParameter")
  fun addCredit(btPinPhoneBuyCreditRequest: BtPinPhoneBuyCreditRequest): Mono<Void> = Mono.empty()
}
