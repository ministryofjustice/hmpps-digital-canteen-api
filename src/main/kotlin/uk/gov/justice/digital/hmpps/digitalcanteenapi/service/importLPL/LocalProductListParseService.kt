package uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL

import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.digitalcanteenapi.config.UpstreamException
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importlpl.LplProductDetails
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.ProductListParser
import java.io.InputStream

@Service
class LocalProductListParseService : ProductListParser<LplProductDetails> {

  object Headers {
    const val TITLE = "DESCRIPTION"
    const val PRODUCT_CODE = "CODE"
    const val PRICE = "COST"
  }

  override fun parse(inputStream: InputStream): List<LplProductDetails> {
    return WorkbookFactory.create(inputStream).use { workbook ->
      val sheet = workbook.getSheetAt(0)
      if (sheet.physicalNumberOfRows == 0) {
        return@use emptyList()
      }
      val formatter = DataFormatter()
      val headerRow = sheet.getRow(0)
        ?: return@use emptyList()

      val headerMap = mutableMapOf<String, Int>()

      for (cell in headerRow) {
        headerMap[
          formatter.formatCellValue(cell).trim(),
        ] = cell.columnIndex
      }

      // validate table headers
      validateHeaders(headerMap)

      val lplProductList = mutableListOf<LplProductDetails>()

      for (rowIndex in 1..sheet.lastRowNum) {
        val row = sheet.getRow(rowIndex) ?: continue

        val productCode = row.value(
          headerMap,
          Headers.PRODUCT_CODE,
          formatter,
        )

        val priceText = row.value(headerMap, Headers.PRICE, formatter)
          .trim()
          .replace("£", "")
          .replace(",", "")

        // Skip empty rows
        if (productCode.isBlank()) {
          continue
        }
        lplProductList.add(
          LplProductDetails(
            productCode = productCode,
            title = row.value(headerMap, Headers.TITLE, formatter).ifBlank { null },
            price = when {
              priceText.isBlank() -> null
              else -> priceText.toDoubleOrNull()?.let { (it * 100).toInt() } ?: throw UpstreamException(
                "Invalid price value '$priceText'",
              )
            },
          ),
        )
      }
      lplProductList
    }
  }

  private fun validateHeaders(headerMap: Map<String, Int>) {
    val requiredHeaders = listOf(
      Headers.PRODUCT_CODE,
      Headers.PRICE,
    )

    val missingHeaders = requiredHeaders.filter {
      !headerMap.containsKey(it)
    }
    require(missingHeaders.isEmpty()) {
      "Missing required header: ${missingHeaders.joinToString(", ")}"
    }
  }

  private fun Row.value(
    headerMap: Map<String, Int>,
    header: String,
    formatter: DataFormatter,
  ): String {
    val index = headerMap[header] ?: return ""

    val cell = getCell(
      index,
      Row.MissingCellPolicy.RETURN_BLANK_AS_NULL,
    ) ?: return ""

    return formatter.formatCellValue(cell).trim()
  }
}
