package uk.gov.justice.digital.hmpps.digitalcanteenapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DigitalCanteenApi

fun main(args: Array<String>) {
  runApplication<DigitalCanteenApi>(*args)
}
