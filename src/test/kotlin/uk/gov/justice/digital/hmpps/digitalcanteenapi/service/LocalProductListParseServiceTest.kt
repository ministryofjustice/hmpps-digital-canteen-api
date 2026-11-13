package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importLPL.LocalProductListParseService
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class LocalProductListParseServiceTest {
  private lateinit var service: LocalProductListParseService

  @BeforeEach
  fun setUp() {
    service = LocalProductListParseService()
  }

  @Test
  fun `should parse products from excel file`() {
    // given
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Local Products List")

    val headerRow = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("DESCRIPTION")
    headerRow.createCell(1).setCellValue("CODE")
    headerRow.createCell(2).setCellValue("COST")

    val dataRow = sheet.createRow(1)
    dataRow.createCell(0).setCellValue("1st Class Stamp")
    dataRow.createCell(1).setCellValue("2345678")
    dataRow.createCell(2).setCellValue("£1.09")

    val outputStream = ByteArrayOutputStream()
    workbook.write(outputStream)
    workbook.close()

    val inputStream = ByteArrayInputStream(outputStream.toByteArray())

    // when
    val result = service.parse(inputStream)

    // then
    assertEquals(1, result.size)

    val product = result.first()

    assertEquals("2345678", product.productCode)
  }
}
