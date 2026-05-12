package uk.gov.justice.digital.hmpps.digitalcanteenapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @param:Value("\${api.hmpps-auth.base-url}") private val hmppsAuthBaseUri: String,
  @param:Value("\${api.hmpps-auth.timeout-ms:20s}") private val hmppsAuthHealthTimeout: Duration,

  @param:Value("\${api.health-and-medication.base-url}") val healthAndMedicationBaseUri: String,
  @param:Value("\${api.health-and-medication.timeout-ms:20s}") val timeout: Duration,

  private val builder: WebClient.Builder,
) {
  @Bean
  @Suppress("MaxLineLength")
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient =
    builder.healthWebClient(hmppsAuthBaseUri, hmppsAuthHealthTimeout)

  @Bean
  fun healthAndMedicationWebClient(authorizedClientManager: OAuth2AuthorizedClientManager) =
    builder.authorisedWebClient(authorizedClientManager, "hmpps-digital-canteen-api",
      healthAndMedicationBaseUri, timeout)

}
