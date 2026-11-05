package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importnpl.NplProduct
import java.io.InputStream

@Service
class NationalProductListParseService {

  object Headers {
    const val IC_CODE = "IC Code"
    const val LPL_ORDERING_CODE = "LPL Ordering Code"
    const val DESCRIPTION = "MCode Description"
    const val SHORT_DESCRIPTION = "Short Description"
    const val EAN_BARCODE = "EAN Barcode"
    const val PRICE_PENCE = "PricePence"
    const val SELECTION_CATEGORY = "Selection Categories"
  }

  fun parse(inputStream: InputStream): List<NplProduct> {
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

      val nplProductList = mutableListOf<NplProduct>()

      for (rowIndex in 1..sheet.lastRowNum) {
        val row = sheet.getRow(rowIndex) ?: continue

        val icCode = row.value(
          headerMap,
          Headers.IC_CODE,
          formatter,
        )

        // Skip empty rows
        if (icCode.isBlank()) {
          continue
        }
        nplProductList.add(
          NplProduct(
            icCode = icCode,
            lplOrderingCode = row.value(headerMap, Headers.LPL_ORDERING_CODE, formatter).ifBlank { null },
            description = row.value(headerMap, Headers.DESCRIPTION, formatter).ifBlank { null },
            shortDescription = row.value(headerMap, Headers.SHORT_DESCRIPTION, formatter).ifBlank { null },
            eanBarcode = row.value(headerMap, Headers.EAN_BARCODE, formatter).let {
              it.toBigDecimalOrNull()?.toPlainString() ?: it
            }.ifBlank { null },
            pricePence = row.value(headerMap, Headers.PRICE_PENCE, formatter).toBigDecimalOrNull(),
            selectionCategories = row.value(headerMap, Headers.SELECTION_CATEGORY, formatter).ifBlank { null },
          ),
        )
      }
      nplProductList
    }
  }

  // TODO : can add mode fields if required
  private fun validateHeaders(headerMap: Map<String, Int>) {
    val requiredHeaders = listOf(
      Headers.IC_CODE,
      Headers.EAN_BARCODE,
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
      MissingCellPolicy.RETURN_BLANK_AS_NULL,
    ) ?: return ""

    return formatter.formatCellValue(cell).trim()
  }
}
