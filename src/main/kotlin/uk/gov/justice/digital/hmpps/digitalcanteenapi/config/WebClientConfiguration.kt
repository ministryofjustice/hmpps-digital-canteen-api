package uk.gov.justice.digital.hmpps.digitalcanteenapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.web.SecurityFilterChain
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

  @param:Value("\${api.prisoner-incentives.base-url}") val prisonerIncentivesBaseUri: String,
  @param:Value("\${api.prisoner-incentives.timeout-ms:20s}") val prisonerIncentivesTimeout: Duration,

  @param:Value("\${api.open-food-facts.base-url}") val openFoodFactsBaseUri: String,
  @param:Value("\${api.open-food-facts.timeout-ms:20s}") val openFoodFactsTimeout: Duration,

  @param:Value("\${api.open-products-facts.base-url}") val openProductsFactsBaseUri: String,
  @param:Value("\${api.open-products-facts.timeout-ms:20s}") val openProductsFactsTimeout: Duration,

  private val builder: WebClient.Builder,
) {
  @Bean
  @Suppress("MaxLineLength")
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, hmppsAuthHealthTimeout)

  @Bean
  fun prisonerHealthAndMedicationWebClient(authorizedClientManager: OAuth2AuthorizedClientManager) = builder.authorisedWebClient(
    authorizedClientManager,
    "hmpps-digital-canteen-api",
    healthAndMedicationBaseUri,
    healthAndMedicationTimeout,
  )

  @Bean
  fun prisonerSearchWebClient(authorizedClientManager: OAuth2AuthorizedClientManager) = builder.authorisedWebClient(
    authorizedClientManager,
    "hmpps-digital-canteen-api",
    prisonerSearchBaseUri,
    prisonerSearchTimeout,
  )

  @Bean
  fun prisonerAdjudicationsWebClient(authorizedClientManager: OAuth2AuthorizedClientManager) = builder.authorisedWebClient(
    authorizedClientManager,
    "hmpps-digital-canteen-api",
    prisonerAdjudicationsBaseUri,
    prisonerAdjudicationsTimeout,
  )

  @Bean
  fun prisonerIncentivesWebClient(authorizedClientManager: OAuth2AuthorizedClientManager) = builder.authorisedWebClient(
    authorizedClientManager,
    "hmpps-digital-canteen-api",
    prisonerIncentivesBaseUri,
    prisonerIncentivesTimeout,
  )

  @Bean
  fun openFoodFactsWebClient(): WebClient = builder.baseUrl(openFoodFactsBaseUri).build()

  @Bean
  fun openProductsFactsWebClient(): WebClient = builder.baseUrl(openProductsFactsBaseUri).build()

  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      csrf { disable() }
      authorizeHttpRequests {
        authorize("/api/product/**", permitAll)
        authorize("/health/**", permitAll)
        authorize("/info", permitAll)
        authorize("/v3/api-docs/**", permitAll)
        authorize("/swagger-ui/**", permitAll)
        authorize("/swagger-ui.html", permitAll)
        authorize(anyRequest, authenticated)
      }
      oauth2ResourceServer { jwt { } }
    }
    return http.build()
  }
}
