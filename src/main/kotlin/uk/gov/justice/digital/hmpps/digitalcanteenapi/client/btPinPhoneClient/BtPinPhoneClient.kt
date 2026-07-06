package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneClientDto

@Component
class BtPinPhoneClient {

  fun getPrisonerBalance(prisonerNumber: String): Mono<BtPinPhoneClientDto> = Mono.just(
    BtPinPhoneClientDto(
      reference = "random reference",
      prisonerId = prisonerNumber,
      balancePence = 12.2,
      creditLimitPounds = 30.0,
    ),
  )
}
