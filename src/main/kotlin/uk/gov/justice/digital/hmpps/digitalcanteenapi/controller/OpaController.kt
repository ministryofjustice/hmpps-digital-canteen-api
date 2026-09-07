package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.logicengine.request.OpaRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.logicengine.response.OpaResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.logicengine.OpaService

@RestController
@PreAuthorize("permitAll()")
@RequestMapping("/api/opa")
@Tag(
  name = "Logic engine",
  description = "Logic engine endpoints to evaluate rules",
)
class OpaController(
  private val opaService: OpaService,
) {

  @Operation(
    summary = "Evaluate rules for PIN phone product",
    description = "Evaluates business rules through OPA."
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Policy successfully evaluated",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = OpaResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request payload"
      ),
      ApiResponse(
        responseCode = "500",
        description = "OPA evaluation failed"
      )
    ]
  )
  @PostMapping("/evaluate")
  fun evaluate(
    @RequestBody request: OpaRequest,
  ): ResponseEntity<OpaResponse> =
    ResponseEntity.ok(
      opaService.evaluatePolicy(request),
    )
}