package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudications.generated.ActivePunishmentDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient.PrisonerAdjudicationsClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.BalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.BtPinPhoneResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.PrisonerIncentivesResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.PrisonerSearchResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.toBalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.toBtPinPhoneResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.toPrisonerIncentiveResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.toPrisonerSearchResponseDto
import java.util.Optional
import java.util.UUID

/**
 * Service responsible for enriching prisoner data with additional information from multiple sources.
 *
 * Aggregates data from various HMPPS APIs,
 * including incentives, adjudications, finance information from NOMIS and BT.
 *
 * @property prisonerSearchClient Client for retrieving basic prisoner information and incentives
 * @property prisonerAdjudicationsClient Client for retrieving prisoner adjudication data
 * @property btPinPhoneClient Client for retrieving prisoner BT PinPhone balance information
 * @property prisonFinanceClient Client for retrieving prisoner finance information
 */
@Service
class PrisonerEnrichmentRulesEnginePoc(
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonerAdjudicationsClient: PrisonerAdjudicationsClient,
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

    val bookingIdMono: Mono<String> =
      prisonerMono.mapNotNull { it.bookingId }

    val activeAdjudicationsMono = bookingIdMono
      .flatMap { bookingId -> prisonerAdjudicationsClient.getPrisonerAdjudication(bookingId) }
      .onErrorResume { Mono.empty() }

    val balanceMono = bookingIdMono
      .flatMap { bookingId -> prisonFinanceClient.getPrisonerBalance(bookingId) }
      .onErrorResume { Mono.empty() }

    val reference = UUID.randomUUID().toString().replace("-", "").take(20)
    val btBalanceMono =
      btPinPhoneClient.getPrisonerBalance(BtPinPhoneBalanceRequest(reference, prisonerNumber))
        .onErrorResume { Mono.empty() }

    return Mono.zip(
      prisonerMono,
      balanceMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
      btBalanceMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
      activeAdjudicationsMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
    )
      .map { tuple ->
        val prisoner = tuple.t1
        val activeAdjudications = tuple.t4.orElse(null)
        EnrichedPinPhonePrisonerDto(
          prisoner = prisoner.toPrisonerSearchResponseDto(),
          incentives = prisoner.currentIncentive?.toPrisonerIncentiveResponseDto(),
          prisonerBalance = tuple.t2.orElse(null)?.toBalanceResponseDto(),
          prisonerBtBalance = tuple.t3.orElse(null)?.toBtPinPhoneResponseDto(),
          hasActiveAdjudications = !activeAdjudications.isNullOrEmpty(),
          activeAdjudications = activeAdjudications?.takeIf { it.isNotEmpty() },
        )
      }
  }

  data class EnrichedPinPhonePrisonerDto(
    val prisoner: PrisonerSearchResponseDto,
    val incentives: PrisonerIncentivesResponseDto?,
    val prisonerBalance: BalanceResponseDto?,
    val prisonerBtBalance: BtPinPhoneResponseDto?,
    val hasActiveAdjudications: Boolean,
    val activeAdjudications: List<ActivePunishmentDto>?,
  )
}
