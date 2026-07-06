package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneClientDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient.PrisonerAdjudicationsClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient.dto.Punishment
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerincentivesclient.PrisonerIncentivesClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerincentivesclient.dto.PrisonerIncentivesDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.dto.PrisonerSearchDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.BalanceDto
import java.util.Optional
import kotlin.collections.isNotEmpty

/**
 * Service responsible for enriching prisoner data with additional information from multiple sources.
 *
 * Aggregates data from various HMPPS APIs,
 * including incentives,  adjudications, finance information from NOMIS and BT.
 *
 * @property prisonerSearchClient Client for retrieving basic prisoner information
 * @property prisonerAdjudicationsClient Client for retrieving prisoner adjudication data
 * @property prisonerIncentivesClient Client for retrieving prisoner incentive information
 * @property btPinPhoneClient Client for retrieving prisoner BT PinPhone balance information
 * @property prisonFinanceClient Client for retrieving prisoner finance information
 */
@Service
class PinPhonePrisonerEnrichmentService(
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonerAdjudicationsClient: PrisonerAdjudicationsClient,
  private val prisonerIncentivesClient: PrisonerIncentivesClient,
  private val btPinPhoneClient: BtPinPhoneClient,
  private val prisonFinanceClient: PrisonFinanceClient,
) {

  /**
   * @param prisonerNumber The unique identifier for the prisoner
   * @return A Mono emitting an [EnrichedPinPhonePrisonerDto] containing aggregated prisoner data
   */
  fun getEnrichedPrisoner(prisonerNumber: String): Mono<EnrichedPinPhonePrisonerDto> {
    val prisonerMono =
      prisonerSearchClient.getPrisoner(prisonerNumber).cache()

    val incentivesMono =
      prisonerIncentivesClient.getPrisoner(prisonerNumber)
        .onErrorResume { Mono.empty() }

    val bookingIdMono: Mono<String> =
      prisonerMono.mapNotNull { it.bookingId }

    val activeAdjudicationsMono = bookingIdMono
      .flatMap { bookingId -> prisonerAdjudicationsClient.getPrisonerAdjudication(bookingId) }
      .onErrorResume { Mono.empty() }

    val balanceMono = bookingIdMono
      .flatMap { bookingId -> prisonFinanceClient.getPrisonerBalance(bookingId) }
      .onErrorResume { Mono.empty() }

    // Bt is currently faked/hardcoded
    val btPinPhoneMono =
      btPinPhoneClient.getPrisonerBalance(prisonerNumber)
        .onErrorResume { Mono.empty() }

    return Mono.zip(
      prisonerMono,
      incentivesMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
      balanceMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
      btPinPhoneMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
      activeAdjudicationsMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),

    )
      .map { tuple ->
        val activeAdjudications = tuple.t5.orElse(null)
        EnrichedPinPhonePrisonerDto(
          prisoner = tuple.t1,
          incentives = tuple.t2.orElse(null),
          prisonerBalance = tuple.t3.orElse(null),
          prisonerBtBalance = tuple.t4.orElse(null),
          hasActiveAdjudications = !activeAdjudications.isNullOrEmpty(),
          activeAdjudications = activeAdjudications?.takeIf { it.isNotEmpty() },
        )
      }
  }

  data class EnrichedPinPhonePrisonerDto(
    val prisoner: PrisonerSearchDto,
    val incentives: PrisonerIncentivesDto?,
    val prisonerBalance: BalanceDto?,
    val prisonerBtBalance: BtPinPhoneClientDto?,
    val hasActiveAdjudications: Boolean,
    val activeAdjudications: List<Punishment>?,
  )
}
