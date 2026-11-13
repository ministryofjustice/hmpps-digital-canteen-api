package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.localproductlistimportapi.generated.ImportLocalProductListResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.LocalProductListSyncService

@RestController
@PreAuthorize("permitAll()")
@RequestMapping("/api/local-product-list/")
class LocalProductListImportController(
  private val syncService: LocalProductListSyncService,
) {
  @Operation(
    summary = "Upload Local Product List",
    description = """
            Accepts an Excel file containing Local Product List data,
            parses the contents, and synchronises products with Medusa.
        """,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "File processed successfully",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid file",
      ),
    ],
  )
  @PostMapping(
    "/{prisonId}/import",
    consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
  )
  fun import(
    @PathVariable prisonId: String,
    @RequestParam("file")
    file: MultipartFile,
  ): ResponseEntity<ImportLocalProductListResponse> = ResponseEntity.ok(syncService.syncProducts(file, prisonId))
}
