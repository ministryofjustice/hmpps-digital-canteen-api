package domains.rules

import rego.v1

#
# Product ID match
#
product_matches(rule) if {

    ids := object.get(rule.products, "ids", [])

    input.productId in ids
}

#
# Category match
#
product_matches(rule) if {

    categories := object.get(rule.products, "categories", [])

    data.products[input.productId].category in categories
}

#
# Attribute match
#
product_matches(rule) if {

    attrs := object.get(rule.products, "attributes", [])

    product_attr := data.products[input.productId].attributes[_]

    product_attr in attrs
}