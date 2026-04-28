package uk.gov.justice.digital.hmpps.digitalcanteenapi.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class TestController {

  @Suppress("FunctionOnlyReturningConstant")
  @PreAuthorize("permitAll()")
  @GetMapping("/test")
  fun testEndpoint(): String = "test"
}
