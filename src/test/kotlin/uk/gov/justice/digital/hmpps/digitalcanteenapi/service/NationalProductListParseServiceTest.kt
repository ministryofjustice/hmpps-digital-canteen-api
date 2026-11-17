package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.digitalcanteenapi.model.importnpl.NplProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.service.importNPL.NationalProductListParseService
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Collections.emptyList

class NationalProductListParseServiceTest {
  private lateinit var service: NationalProductListParseService

  @BeforeEach
  fun setUp() {
    service = NationalProductListParseService()
  }

  @Test
  fun `should parse products from excel file`() {
    // given
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("National Products List")

    val headerRow = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("IC Code")
    headerRow.createCell(1).setCellValue("LPL Ordering Code")
    headerRow.createCell(2).setCellValue("Description")
    headerRow.createCell(3).setCellValue("Short Description")
    headerRow.createCell(4).setCellValue("EAN Barcode")
    headerRow.createCell(5).setCellValue("Price Pence")
    headerRow.createCell(6).setCellValue("Selection Categories")

    val dataRow = sheet.createRow(1)
    dataRow.createCell(0).setCellValue("1001")
    dataRow.createCell(1).setCellValue("MC10001")
    dataRow.createCell(2).setCellValue("1p Postage Stamp")
    dataRow.createCell(3).setCellValue("1p Postage Stamp Sheet  25s")
    dataRow.createCell(4).setCellValue("1223456657")
    dataRow.createCell(5).setCellValue("100")
    dataRow.createCell(6).setCellValue("STAMPS")

    val outputStream = ByteArrayOutputStream()
    workbook.write(outputStream)
    workbook.close()

    val inputStream = ByteArrayInputStream(outputStream.toByteArray())

    // when
    val result = service.parse(inputStream)

    // then
    assertEquals(1, result.size)

    val product = result.first()

    assertEquals("1001", product.icCode)
  }

  @Test
  fun `should return empty list for empty sheet`() {
    // given
    val workbook = XSSFWorkbook()
    workbook.createSheet("Products")

    val outputStream = ByteArrayOutputStream()
    workbook.write(outputStream)
    workbook.close()

    val inputStream = ByteArrayInputStream(outputStream.toByteArray())

    // when
    val result = service.parse(inputStream)

    // then
    assertEquals(emptyList<NplProduct>(), result)
  }

  @Test
  fun `should throw exception when required header is missing`() {
    // given
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Products")

    val headerRow = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("IC Code")
    headerRow.createCell(1).setCellValue("LPL Ordering Code")
    headerRow.createCell(2).setCellValue("Description")
    headerRow.createCell(3).setCellValue("Short Description")
    headerRow.createCell(5).setCellValue("Price Pence")
    headerRow.createCell(6).setCellValue("Selection Categories")

    val outputStream = ByteArrayOutputStream()
    workbook.write(outputStream)
    workbook.close()

    val inputStream = ByteArrayInputStream(outputStream.toByteArray())
    // when
    val exception = assertThrows<IllegalArgumentException> {
      service.parse(inputStream)
    }
    // then
    assertEquals("Missing required header: EAN Barcode", exception.message)
  }

  @Test
  fun `should return empty list for missing header row`() {
    // given
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("National Products List")

    val dataRow = sheet.createRow(1)
    dataRow.createCell(0).setCellValue("1001")
    dataRow.createCell(1).setCellValue("MC10001")
    dataRow.createCell(2).setCellValue("1p Postage Stamp")
    dataRow.createCell(3).setCellValue("1p Postage Stamp Sheet  25s")
    dataRow.createCell(4).setCellValue("1223456657")
    dataRow.createCell(5).setCellValue("100")
    dataRow.createCell(6).setCellValue("STAMPS")

    val outputStream = ByteArrayOutputStream()
    workbook.write(outputStream)
    workbook.close()

    val inputStream = ByteArrayInputStream(outputStream.toByteArray())

    // when
    val result = service.parse(inputStream)

    // then
    assertEquals(0, result.size)
  }
}
