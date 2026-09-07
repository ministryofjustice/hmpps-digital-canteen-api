package uk.gov.justice.digital.hmpps.digitalcanteenapi.service.logicengine

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.opa.OpaClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.logicengine.request.OpaRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.logicengine.response.OpaResponse

@Service
class OpaService(
  private val opaClient: OpaClient,
) {

  fun evaluatePolicy(request: OpaRequest): OpaResponse =
    opaClient.evaluate(request)
}