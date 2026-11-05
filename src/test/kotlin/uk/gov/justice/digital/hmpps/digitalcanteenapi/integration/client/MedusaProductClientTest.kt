package uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.client

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.WebClientErrorHandler
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.CreateProduct
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductMetadata
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaProductClient
import uk.gov.justice.digital.hmpps.digitalcanteenapi.integration.wiremock.MedusaMockServer
import java.util.Collections.emptyList

class MedusaProductClientTest {
  private lateinit var client: MedusaProductClient

  @BeforeEach
  fun resetMocks() {
    server.resetRequests()
    val webClient = WebClient.create("http://localhost:${server.port()}")
    val mapper = JsonMapper.builder()
      .findAndAddModules()
      .build()
    val webClientErrorHandler = WebClientErrorHandler(mapper)
    client = MedusaProductClient(webClient, webClientErrorHandler, mapper)
  }

  @Test
  fun `getProducts- successfully get the existing products from medusa`() {
    server.stubGetProducts(
      """
        {
          "products": [
            {
              "id": "prod_123",
              "handle": "IC001",
              "title": "test-product"
            }
          ]
        }
      """.trimIndent(),
    )

    val response = client.getProducts()

    assertEquals(1, response.products.size)
    assertEquals("prod_123", response.products.first().id)
    assertEquals("IC001", response.products.first().handle)
  }

  @Test
  fun `getProducts - throws exception for unauthorized response`() {
    server.stubGetProductsWithStatus(
      status = 401,
      responseBody = """
            {
              "message":"Unauthorized"
            }
      """.trimIndent(),
    )

    assertThrows<Exception> {
      client.getProducts()
    }
  }

  @Test
  fun `batchProducts - should create products successfully`() {
    server.stubBatchProducts(
      """
    {
      "created": [
        {
          "id": "prod_`1",
          "title": "test-product",
          "description": "sss",
          "handle": "IC001",
          "status": "published",
          "metadata": {
            "icCode": "275133",
            "eanBarcode": "123456789",
            "lplOrderingCode": "M120987",
            "selectionCategory": "CAT"
          }
        }
      ],
      "updated": [],
      "deleted": {
        "ids": [],
        "object": null,
        "deleted": false
      }
    }
      """.trimIndent(),
    )

    val response = client.batchProducts(
      ProductBatchRequest(
        create = listOf(
          CreateProduct(
            title = "test-product",
            handle = "IC001",
            description = "test-product-description",
            status = "Published",
            options = emptyList(),
            variants = emptyList(),
            subtitle = "test-product-subtitle",
            metadata = ProductMetadata(
              icCode = "IC001",
              lplOrderingCode = "",
              eanBarcode = "123456789",
              selectionCategory = "CAT",
            ),
          ),
        ),
      ),
    )

    assertEquals(1, response.created.size)
    assertEquals(0, response.updated.size)
    assertEquals(0, response.deleted.ids.size)
  }
  companion object {
    @JvmField
    internal val server = MedusaMockServer()

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
