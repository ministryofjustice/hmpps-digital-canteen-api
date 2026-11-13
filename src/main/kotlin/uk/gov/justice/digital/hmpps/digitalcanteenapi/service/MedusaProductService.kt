package uk.gov.justice.digital.hmpps.digitalcanteenapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.createproduct.generated.ProductBatchRequest
import uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.MedusaProductClient

@Service
class MedusaProductService(
  private val medusaProductClient: MedusaProductClient,
) {

  fun getProducts(limit: Int, offset: Int) = medusaProductClient.getProducts(limit, offset)

  fun batchProducts(request: ProductBatchRequest) = medusaProductClient.batchProducts(request)
}
