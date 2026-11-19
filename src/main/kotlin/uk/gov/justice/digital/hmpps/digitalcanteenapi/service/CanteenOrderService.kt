package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CancelDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetDraftOrderResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaInternalClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.BalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.toBalanceResponseDto
import kotlin.String

/**
 *
 * @property prisonerSearchClient Client for retrieving basic prisoner information and incentives
 * @property prisonFinanceClient Client for retrieving prisoner finance information
 */
@Service
class CanteenOrderService(
  private val financeService: PrisonFinanceService,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonFinanceClient: PrisonFinanceClient,
  private val medusaInternalClient: MedusaInternalClient,
) {

  companion object {
    private val log: Logger = LoggerFactory.getLogger("CanteenOrderService")
  }

  /**
   * @param prisonerNumber The unique identifier for the prisoner
   * @return A Mono emitting an [PrisonerBalanceDto] containing prisoner balance information
   */
  fun getPrisonerBalance(prisonerNumber: String): Mono<BalanceResponseDto> {
    val prisonerMono = prisonerSearchClient.getPrisoner(prisonerNumber).cache()

    val bookingIdMono: Mono<String> = prisonerMono.mapNotNull { it.bookingId }

    return bookingIdMono
      .flatMap { bookingId -> prisonFinanceClient.getPrisonerBalance(bookingId) }
      .map { it.toBalanceResponseDto() }
  }

  /**
   * @param paymentHoldRequest
   * @return A Mono emitting an [PrisonerBalanceDto] containing prisoner balance information
   *
   */
  fun createDraftOrder(createDraftOrderRequest: CreateDraftOrderRequest, cartId: String): CreateDraftOrderResponse = medusaInternalClient.createDraftOrder(createDraftOrderRequest, cartId)

  /**
   * @param paymentHoldRequest
   * @return A Mono emitting an [PrisonerBalanceDto] containing prisoner balance information
   *
   */
  fun getDraftOrder(orderId: String): GetDraftOrderResponse = medusaInternalClient.getDraftOrder(orderId)

  /**
   * @param paymentHoldRequest
   * @return A Mono emitting an [PrisonerBalanceDto] containing prisoner balance information
   *
   */
  fun cancelOrder(cancelDraftOrderRequest: CancelDraftOrderRequest) {
    println(cancelDraftOrderRequest)
    medusaInternalClient.cancelOrder(cancelDraftOrderRequest)
  }
}
