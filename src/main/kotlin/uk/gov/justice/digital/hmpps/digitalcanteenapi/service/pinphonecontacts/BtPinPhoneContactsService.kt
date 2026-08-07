package uk.gov.justice.digital.hmpps.digitalcanteenapi.service.pinphonecontacts

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.BtPinPhoneClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.BtPinPhoneControlledNumbersRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated.ControlledNumber
import java.util.UUID

/**
 * Service responsible for retrieving prisoner contacts from BT PinPhone
 *
 * @property btPinPhoneClient Client for retrieving prisoner contact information
 */
@Service
class BtPinPhoneContactsService(
  private val btPinPhoneClient: BtPinPhoneClient,
) {

  /**
   * @param prisonerNumber The unique identifier for the prisoner
   * @return A Mono emitting an [BtPinPhoneContactDto] containing list of prisoner contacts
   */
  fun getPrisonerContacts(prisonerNumber: String): Mono<List<BtPinPhoneContactDto>> {
    val reference = "${UUID.randomUUID()}-$prisonerNumber"
    return btPinPhoneClient.getPrisonerContacts(
      BtPinPhoneControlledNumbersRequest(reference, prisonerNumber),
    )
      .map { response ->
        response.controlledNumbers.map { it.toContactDto(response.prisonerId) }
      }
  }

  private fun ControlledNumber.toContactDto(prisonerId: String) = BtPinPhoneContactDto(
    prisonerId = prisonerId,
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    controlStatus = controlStatus,
    callAllowed = callAllowed,
    legal = legal,
    allowMonitor = allowMonitor,
    alert = alert,
    override = override,
    contactType = BtRelationshipType.fromId(relationshipId).category.name,
    contactTypeDescription = BtRelationshipType.fromId(relationshipId).description,
  )

  data class BtPinPhoneContactDto(
    val prisonerId: String,
    val id: Int,
    val name: String,
    val phoneNumber: String,
    val controlStatus: Boolean,
    val callAllowed: Boolean,
    val legal: Boolean,
    val allowMonitor: Boolean,
    val alert: Boolean,
    val override: Boolean,
    val contactType: String,
    val contactTypeDescription: String,
  )
}
