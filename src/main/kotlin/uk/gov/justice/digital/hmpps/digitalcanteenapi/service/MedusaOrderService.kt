package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CancelDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.CreateDraftOrderResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaapiclient.generated.GetDraftOrderResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaOrderClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonersearchclient.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.PrisonFinanceClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.BalanceResponseDto
import uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping.prisonerenrichment.toBalanceResponseDto
import kotlin.String

@Service
class MedusaOrderService(
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonFinanceClient: PrisonFinanceClient,
  private val medusaOrderClient: MedusaOrderClient,
) {

  companion object {
    private val log: Logger = LoggerFactory.getLogger("MedusaOrderService")
  }

  // todo: just initial, will be refined later
  fun getPrisonerBalance(prisonerNumber: String): Mono<BalanceResponseDto> {
    val prisonerMono = prisonerSearchClient.getPrisoner(prisonerNumber).cache()

    val bookingIdMono: Mono<String> = prisonerMono.mapNotNull { it.bookingId }

    return bookingIdMono
      .flatMap { bookingId -> prisonFinanceClient.getPrisonerBalance(bookingId) }
      .map { it.toBalanceResponseDto() }
  }

  fun createDraftOrder(createDraftOrderRequest: CreateDraftOrderRequest, cartId: String): CreateDraftOrderResponse {
    // todo: payment calls tbc

    return medusaOrderClient.createDraftOrder(createDraftOrderRequest, cartId)
  }

  fun getDraftOrder(orderId: String): GetDraftOrderResponse = medusaOrderClient.getDraftOrder(orderId)

  fun cancelOrder(cancelDraftOrderRequest: CancelDraftOrderRequest): ResponseEntity<Map<String, String>> {
    // todo: payment calls tbc

    return medusaOrderClient.cancelOrder(cancelDraftOrderRequest)
  }
}
