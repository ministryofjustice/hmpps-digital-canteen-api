package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import java.io.InputStream

interface ProductListParser<T> {
  fun parse(inputStream: InputStream): List<T>
}