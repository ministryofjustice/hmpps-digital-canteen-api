package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.natioanlproductupload.generated.ProductSyncResponse
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.NationalProductListSyncService

@RestController
@PreAuthorize("permitAll()")
@RequestMapping("/api/national-product-list/")
class NationalProductListImportController(
  private val productSyncService: NationalProductListSyncService,
) {
  @Operation(
    summary = "Upload National Product List",
    description = """
            Accepts an Excel file containing National Product List data,
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
    "/import",
    consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
  )
  fun upload(
    @RequestParam("file")
    file: MultipartFile,
  ): ProductSyncResponse {
    val response = productSyncService.syncProducts(file)
    return ProductSyncResponse(
      recordsCreated = response.created.size,
      recordsUpdated = response.updated.size,
      recordsDeleted = response.deleted.ids.size,
      message = "File uploaded successfully",
    )
  }
}
