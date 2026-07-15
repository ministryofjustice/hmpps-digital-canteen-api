package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneClientDto

@Component
class BtPinPhoneClient {

  fun getPrisonerBalance(prisonerNumber: String): Mono<BtPinPhoneClientDto> = Mono.just(
    BtPinPhoneClientDto(
      reference = "reference_FN",
      prisonerId = prisonerNumber,
      balancePence = 1220,
      creditLimitPence = 300,
    ),
  )
}
