package uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphoneenrichment

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.BalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.PrisonerSearchResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.toBalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.toPrisonerSearchResponseDto
import java.util.*

/**
 * Service responsible for enriching prisoner data with additional information from multiple sources.
 *
 * Aggregates data from various HMPPS APIs and BT,
 * Returns the aggregated balance and credit information
 *
 * @property prisonerSearchClient Client for retrieving basic prisoner information and incentives
 * @property btPinPhoneClient Client for retrieving prisoner BT PinPhone balance information
 * @property prisonFinanceClient Client for retrieving prisoner finance information
 */
@Service
class PinPhonePrisonerEnrichmentService(
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonFinanceClient: PrisonFinanceClient,
) {

  /**
   * @param prisonerNumber The unique identifier for the prisoner
   * @return A Mono emitting an [EnrichedPinPhonePrisonerDto] containing aggregated prisoner data
   */
  fun getEnrichedPrisoner(prisonerNumber: String): Mono<EnrichedPinPhonePrisonerDto> {
    val prisonerMono = prisonerSearchClient.getPrisoner(prisonerNumber).cache()

    val bookingIdMono: Mono<String> = prisonerMono.mapNotNull { it.bookingId }

    val balanceMono = bookingIdMono
      .flatMap { bookingId -> prisonFinanceClient.getPrisonerBalance(bookingId) }

    return Mono.zip(prisonerMono, balanceMono)
      .map { tuple ->
        EnrichedPinPhonePrisonerDto(
          prisoner = tuple.t1.toPrisonerSearchResponseDto(),
          prisonerBalance = tuple.t2.toBalanceResponseDto(),
        )
      }
  }

  data class EnrichedPinPhonePrisonerDto(
    val prisoner: PrisonerSearchResponseDto,
    val prisonerBalance: BalanceResponseDto?,
  )
}
