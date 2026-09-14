package uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
//import org.springframework.web.reactive.function.client.WebClientRequestException
//import org.springframework.web.reactive.function.client.WebClientResponseException
//import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.AccountCreditRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.AccountCreditResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneBalanceResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneControlledNumbersRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneControlledNumbersResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtTokenResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.ControlledNumber

@Component
class BtPinPhoneClient(
  @Qualifier("btPinPhoneWebClient") private val btPinPhoneWebClient: WebClient,
  @Value("\${bt.client.id}") private val clientId: String,
  @Value("\${bt.client.secret}") private val clientSecret: String,
  private val errorHandler: WebClientErrorHandler,
) {

  companion object {
    private val logger = LoggerFactory.getLogger(BtPinPhoneClient::class.java)
  }

//  fun getBtToken(): Mono<BtTokenResponse> = btPinPhoneWebClient
//    .post()
//    .uri("/auth/token")
//    .bodyValue(BtTokenRequest(clientId = clientId, clientSecret = clientSecret))
//    .retrieve()
//    .bodyToMono<BtTokenResponse>()
//    .onErrorMap(WebClientResponseException::class.java) { ex ->
//      val error = errorHandler.handleError(ex)
//      logger.error("BT auth token request failed: ${error.userMessage}")
//      UpstreamException(error.userMessage ?: "Auth token request failed")
//    }
//    .onErrorMap(WebClientRequestException::class.java) { ex ->
//      logger.error("Get BT token has failed due to connection issue", ex)
//      UpstreamException("BT service is currently unavailable")
//    }

//  fun getPrisonerBalance(btPinPhoneBalanceRequest: BtPinPhoneBalanceRequest): Mono<BtPinPhoneBalanceResponse> = getBtToken().flatMap { btAuthResponse ->
//    btPinPhoneWebClient
//      .post()
//      .uri("/pcs/Balance")
//      .headers { it.setBearerAuth(btAuthResponse.accessToken) }
//      .bodyValue(btPinPhoneBalanceRequest)
//      .retrieve()
//      .bodyToMono<BtPinPhoneBalanceResponse>()
//      .onErrorMap(WebClientResponseException::class.java) { ex ->
//        val error = errorHandler.handleError(ex)
//        logger.error("BT balance request failed for prisoner ${btPinPhoneBalanceRequest.prisonerId}: ${ex.responseBodyAsString}")
//        UpstreamException(error.userMessage ?: "Balance request failed")
//      }
//  }

//  fun getPrisonerContacts(btPinPhoneControlledNumbersRequest: BtPinPhoneControlledNumbersRequest): Mono<BtPinPhoneControlledNumbersResponse> = getBtToken().flatMap { btAuthResponse ->
//    btPinPhoneWebClient
//      .post()
//      .uri("/pcs/ControlledNumbers")
//      .headers { it.setBearerAuth(btAuthResponse.accessToken) }
//      .bodyValue(btPinPhoneControlledNumbersRequest)
//      .retrieve()
//      .bodyToMono<BtPinPhoneControlledNumbersResponse>()
//      .onErrorMap(WebClientResponseException::class.java) { ex ->
//        val error = errorHandler.handleError(ex)
//        logger.error("BT contacts request failed for prisoner ${btPinPhoneControlledNumbersRequest.prisonerId}: ${ex.responseBodyAsString}")
//        UpstreamException(error.userMessage ?: "Contacts request failed")
//      }
//  }

//  fun addCredit(accountCreditRequest: AccountCreditRequest): Mono<AccountCreditResponse> = getBtToken().flatMap { btAuthResponse ->
//    btPinPhoneWebClient
//      .post()
//      .uri("/pcs/AccountCredit")
//      .headers { it.setBearerAuth(btAuthResponse.accessToken) }
//      .bodyValue(accountCreditRequest)
//      .retrieve()
//      .bodyToMono(AccountCreditResponse::class.java)
//      .onErrorMap(WebClientResponseException::class.java) { ex ->
//        val error = errorHandler.handleError(ex)
//        logger.error("BT add credit request failed for prisoner ${accountCreditRequest.prisonerId}: ${ex.responseBodyAsString}")
//        UpstreamException(error.userMessage ?: "Add credit failed")
//      }
//  }

  fun getBtToken(): Mono<BtTokenResponse> = Mono.just(btTokenResponse)

  fun getPrisonerBalance(btPinPhoneBalanceRequest: BtPinPhoneBalanceRequest): Mono<BtPinPhoneBalanceResponse> = Mono.just(btPinPhoneBalanceResponse)

  fun getPrisonerContacts(btPinPhoneControlledNumbersRequest: BtPinPhoneControlledNumbersRequest): Mono<BtPinPhoneControlledNumbersResponse> = Mono.just(btPinPhoneControlledNumbersResponse)

  fun addCredit(accountCreditRequest: AccountCreditRequest): Mono<AccountCreditResponse> = Mono.just(accountCreditResponse)

  val btTokenResponse = BtTokenResponse(
    accessToken = "mocked-bt-token",
    tokenType = "test",
    expiresIn = 3600,
  )

  val btPinPhoneBalanceResponse = BtPinPhoneBalanceResponse(
    reference = "mocked-bt-ref",
    prisonerId = "test",
    balancePence = 3600,
    creditLimitPence = 5000,
  )

  val btPinPhoneControlledNumbersResponse = BtPinPhoneControlledNumbersResponse(
    reference = "ref124",
    prisonerId = "test",
    controlledNumbers = listOf(
      ControlledNumber(
        id = 162439,
        name = "John Doe",
        phoneNumber = "07700900351",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 2,
      ),
      ControlledNumber(
        id = 162440,
        name = "Jane Smith",
        phoneNumber = "07700900352",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 1,
      ),
      ControlledNumber(
        id = 162441,
        name = "Robert Brown",
        phoneNumber = "07700900353",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 3,
      ),
      ControlledNumber(
        id = 162442,
        name = "Sarah Williams",
        phoneNumber = "07700900354",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 4,
      ),
      ControlledNumber(
        id = 162443,
        name = "Michael Jones",
        phoneNumber = "07700900355",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 5,
      ),
      ControlledNumber(
        id = 162444,
        name = "Emma Taylor",
        phoneNumber = "07700900356",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 7,
      ),
      ControlledNumber(
        id = 162445,
        name = "David Wilson",
        phoneNumber = "07700900357",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 9,
      ),
      ControlledNumber(
        id = 162446,
        name = "Lisa Davies",
        phoneNumber = "07700900358",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 8,
      ),
      ControlledNumber(
        id = 162447,
        name = "James Evans",
        phoneNumber = "07700900359",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 6,
      ),
      ControlledNumber(
        id = 162448,
        name = "Margaret Thomas",
        phoneNumber = "07700900360",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 15,
      ),
      ControlledNumber(
        id = 162449,
        name = "Peter Robinson",
        phoneNumber = "07700900361",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 14,
      ),
      ControlledNumber(
        id = 162450,
        name = "Susan Clarke",
        phoneNumber = "07700900362",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 10,
      ),
      ControlledNumber(
        id = 162458,
        name = "Bob Drummond",
        phoneNumber = "07700900367",
        controlStatus = true,
        callAllowed = true,
        legal = true,
        allowMonitor = false,
        alert = true,
        override = true,
        relationshipId = 28,
      ),
    ),
  )

  val accountCreditResponse = AccountCreditResponse(
    reference = "12345",
    prisonerId = "test",
    creditLimitPence = 5000,
    preBalancePence = 1000,
    newBalancePence = 1100,
  )
}
