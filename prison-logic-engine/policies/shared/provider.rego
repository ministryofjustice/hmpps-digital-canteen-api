package shared.provider
import rego.v1

provider_matches(rule) if {
    not rule.providers
}

provider_matches(rule) if {
    input.provider in rule.providers
}