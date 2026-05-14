package uk.gov.justice.digital.hmpps.digitalcanteenapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @param:Value("\${hmpps-auth.url}") val hmppsAuthBaseUri: String,
  @param:Value("\${openfoodfacts.api.url}") val openFoodFactsApiUrl: String,
  @param:Value("\${openproductsfacts.api.url}") val openProductsFactsApiUrl: String,
  @param:Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @param:Value("\${api.timeout:20s}") val timeout: Duration,
  private val builder: WebClient.Builder,
) {
  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  @Bean
  @Suppress("MaxLineLength")
  fun hmppsAuthHealthWebClient(): WebClient =
    builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)


  @Bean
  fun openFoodFactsWebClient(): WebClient =
    builder.baseUrl(openFoodFactsApiUrl).build()

  @Bean
  fun openProductsFactsWebClient(): WebClient =
    builder.baseUrl(openProductsFactsApiUrl).build()

  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      csrf { disable() }
      authorizeHttpRequests {
        authorize("/api/product/**", permitAll)
        authorize("/health/**", permitAll)
        authorize(anyRequest, authenticated)
      }
      oauth2ResourceServer { jwt { } }
    }
    return http.build()
  }

}