package uk.gov.justice.digital.hmpps.digitalcanteenapi.mapping

import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.CartMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.dto.MedusaCreateCartRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.generated.CreateCartRequest

fun CreateCartRequest.toMedusaCreateCartRequest() =
  MedusaCreateCartRequest(
    metadata = CartMetadata(
      prison_id = prisonId,
      offender_no = offenderNo,
      first_name = firstName,
      last_name = lastName,
    ),
  )