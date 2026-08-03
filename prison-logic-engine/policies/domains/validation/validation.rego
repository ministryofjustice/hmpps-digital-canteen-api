package domains.validation

import rego.v1

global_product if {
    data.products[input.productId].global == true
}

errors contains "Invalid prison" if {

    not global_product

    not data.prisons[input.prisoner.prisonId]
}

errors contains "Invalid product" if {
    not data.products[input.productId]
}

errors contains "Missing IEP Code" if {
    input.productId == "BT_PIN_Phone"
    not input.incentives.iepCode
}