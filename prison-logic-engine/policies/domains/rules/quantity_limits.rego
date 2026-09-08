package domains.rules

import rego.v1

import data.domains.rules.applicable_rules

default quantity_limit_exceeded := false

quantity_limit_exceeded if {

    rule_id := applicable_rules[_]

    rule := data.rules[_]

    max_qty := object.get(
        rule,
        "maxQuantityPerCycle",
        999999
    )

    quantity := object.get(
        input,
        "quantity",
        0
    )

    quantity > max_qty
}