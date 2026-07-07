package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.dto.BtPinPhoneClientDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient.PrisonerAdjudicationsClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudicationsclient.dto.Punishment
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.dto.IncentivesDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.dto.PrisonerSearchDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.dto.BalanceDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

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
class PinPhonePrisonerEnrichmentService(
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

    // Bt is currently faked/hardcoded
    val btPinPhoneMono =
      btPinPhoneClient.getPrisonerBalance(prisonerNumber)
        .onErrorResume { Mono.empty() }

    return Mono.zip(
      prisonerMono,
      balanceMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
      btPinPhoneMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
      activeAdjudicationsMono.map { Optional.of(it) }.defaultIfEmpty(Optional.empty()),
    )
      .map { tuple ->
        val prisoner = tuple.t1
        val activeAdjudications = tuple.t4.orElse(null)
        EnrichedPinPhonePrisonerDto(
          prisoner = prisoner.toPrisonerSearchResponseDto(),
          incentives = prisoner.currentIncentive.toPrisonerIncentiveResponseDto(),
          prisonerBalance = tuple.t2.orElse(null),
          prisonerBtBalance = tuple.t3.orElse(null),
          hasActiveAdjudications = !activeAdjudications.isNullOrEmpty(),
          activeAdjudications = activeAdjudications?.takeIf { it.isNotEmpty() },
        )
      }
  }

  data class EnrichedPinPhonePrisonerDto(
    val prisoner: PrisonerSearchResponseDto,
    val incentives: PrisonerIncentivesResponseDto,
    val prisonerBalance: BalanceDto?,
    val prisonerBtBalance: BtPinPhoneClientDto?,
    val hasActiveAdjudications: Boolean,
    val activeAdjudications: List<Punishment>?,
  )

  data class PrisonerSearchResponseDto(
    val prisonerNumber: String,
    val prisonId: String,
    val prisonName: String,
    val bookNumber: String,
    val bookingId: String,
    val dateOfBirth: LocalDate,
    val youthOffender: Boolean,
    val gender: String,
  )

  data class PrisonerIncentivesResponseDto(
    val code: String,
    val description: String,
    val dateTime: LocalDateTime,
    val nextReviewDate: LocalDate,
  )

  private fun PrisonerSearchDto.toPrisonerSearchResponseDto() = PrisonerSearchResponseDto(
    prisonerNumber = prisonerNumber,
    prisonId = prisonId,
    prisonName = prisonName,
    bookNumber = bookNumber,
    bookingId = bookingId,
    dateOfBirth = dateOfBirth,
    youthOffender = youthOffender,
    gender = gender,
  )

  private fun IncentivesDto.toPrisonerIncentiveResponseDto() = PrisonerIncentivesResponseDto(
    code = level.code,
    description = level.description,
    dateTime = dateTime,
    nextReviewDate = nextReviewDate,
  )
}
