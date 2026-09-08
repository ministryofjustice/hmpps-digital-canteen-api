package domains.rules.scope

import rego.v1

rule_applies(rule) if {
    rule.scope == "ALL_PRISONS"
}

rule_applies(rule) if {
    rule.scope == "SPECIFIC_PRISON"
    input.prisoner.prisonId in rule.prisons
}