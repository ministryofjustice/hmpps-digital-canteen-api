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
  @param:Value("\${api.health-and-medication.timeout-ms:20s}") val healthAndMedicationTimeout: Duration,

  @param:Value("\${api.prisoner-search.base-url}") val prisonerSearchBaseUri: String,
  @param:Value("\${api.prisoner-search.timeout-ms:20s}") val prisonerSearchTimeout: Duration,

  @param:Value("\${api.prisoner-adjudications.base-url}") val prisonerAdjudicationsBaseUri: String,
  @param:Value("\${api.prisoner-adjudications.timeout-ms:20s}") val prisonerAdjudicationsTimeout: Duration,

  @param:Value("\${api.prisoner-incentives.base-url}") val prisonerIncentivessBaseUri: String,
  @param:Value("\${api.prisoner-incentives.timeout-ms:20s}") val prisonerIncentivessTimeout: Duration,

  private val builder: WebClient.Builder,
) {
  @Bean
  @Suppress("MaxLineLength")
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder
    .healthWebClient(hmppsAuthBaseUri, hmppsAuthHealthTimeout)

  @Bean
  fun prisonerHealthAndMedicationWebClient(authorizedClientManager: OAuth2AuthorizedClientManager) =
    builder.authorisedWebClient(authorizedClientManager, "hmpps-digital-canteen-api",
      healthAndMedicationBaseUri, healthAndMedicationTimeout)

  @Bean
  fun prisonerSearchWebClient(authorizedClientManager: OAuth2AuthorizedClientManager) =
    builder.authorisedWebClient(authorizedClientManager, "hmpps-digital-canteen-api",
      prisonerSearchBaseUri, prisonerSearchTimeout)

  @Bean
  fun prisonerAdjudicationsWebClient(authorizedClientManager: OAuth2AuthorizedClientManager) =
    builder.authorisedWebClient(authorizedClientManager, "hmpps-digital-canteen-api",
      prisonerAdjudicationsBaseUri, prisonerAdjudicationsTimeout)

  @Bean
  fun prisonerIncentivesWebClient(authorizedClientManager: OAuth2AuthorizedClientManager) =
    builder.authorisedWebClient(authorizedClientManager, "hmpps-digital-canteen-api",
      prisonerIncentivessBaseUri, prisonerIncentivessTimeout)
}
