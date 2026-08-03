package domains.rules

import rego.v1

import data.domains.canteen.credit_limit_exceeded
import data.domains.rules.quantity_limit_exceeded

#
# Default decision
#
default final_decision := "ALLOW"

#
# Any ALLOW exception rule matched
# Example:
# R004 - Religious Artefact Allowance
# R005 - Religious Vape Allowance
#
allow_override if {

    rule_id := applicable_rules[_]

    rule := data.rules[_]

    rule.ruleId == rule_id

    rule.effect == "ALLOW"
}

#
# Any DENY rule matched
# Example:
# R001 - Under 18 Vape
# R002 - Adjudication
# R003 - Arson Charge
#
deny_rule_matched if {

    rule_id := applicable_rules[_]

    rule := data.rules[_]

    rule.ruleId == rule_id

    rule.effect == "DENY"
}

#
# Quantity exceeded always DENY
#
final_decision := "DENY" if {
    quantity_limit_exceeded
}

#
# BT Credit limit exceeded
#
final_decision := "DENY" if {
    credit_limit_exceeded
}

#
# Explicit ALLOW exception wins
#
final_decision := "ALLOW" if {

    allow_override

    not quantity_limit_exceeded
}

#
# Generic DENY
#
final_decision := "DENY" if {

    deny_rule_matched

    not allow_override

    not quantity_limit_exceeded
}

#
# Default ALLOW
#
final_decision := "ALLOW" if {

    not deny_rule_matched

    not credit_limit_exceeded

    not quantity_limit_exceeded
}