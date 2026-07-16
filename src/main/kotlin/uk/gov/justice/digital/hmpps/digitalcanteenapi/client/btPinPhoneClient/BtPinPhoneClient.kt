package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneBalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneBuyCreditRequest

@Component
class BtPinPhoneClient {

  fun getPrisonerBalance(prisonerNumber: String): Mono<BtPinPhoneBalanceResponseDto> = Mono.just(
    BtPinPhoneBalanceResponseDto(
      reference = "reference_FN",
      prisonerId = prisonerNumber,
      balancePence = 1220,
      creditLimitPence = 300,
    ),
  )

  fun addCredit(btPinPhoneBuyCreditRequest: BtPinPhoneBuyCreditRequest): Mono<Void> = Mono.empty()
}
