package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.PrisonerHealthAndMedicationClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.PrisonerHealthAndMediationMockServer

class PrisonerHealthAndMediationClientTest {
  private lateinit var client: PrisonerHealthAndMedicationClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    client = PrisonerHealthAndMedicationClient(webClient)
  }

  @Test
  fun `getPrisoner - successfully returns prisoner with dietary requirements`() {
    server.stubGetPrisoner()

    val result = client.getPrisoner(PRISONER_NUMBER).block()

    assertThat(result).isNotNull

    with(result!!.dietAndAllergy) {
      assertThat(foodAllergies?.value).hasSize(1)
      assertThat(foodAllergies?.value?.map { it.value.id })
        .containsExactly("FOOD_ALLERGY_NUTS")

      assertThat(medicalDietaryRequirements?.value).hasSize(1)
      assertThat(medicalDietaryRequirements?.value?.first()?.value?.id)
        .isEqualTo("DIET_GLUTEN_FREE")

      assertThat(personalisedDietaryRequirements?.value).hasSize(1)
      assertThat(personalisedDietaryRequirements?.value?.first()?.value?.id)
        .isEqualTo("DIET_HALAL")

      assertThat(cateringInstructions?.value)
        .isEqualTo("Cake")

    }
  }

  companion object {
    @JvmField
    internal val server = PrisonerHealthAndMediationMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      server.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      server.stop()
    }
  }
}