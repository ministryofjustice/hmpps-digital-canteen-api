package app.main

import rego.v1


import data.domains.validation.errors
import data.domains.rules.final_decision

import data.domains.canteen.effective_limit
import data.domains.canteen.credit_limit_enabled
import data.domains.canteen.allowed_fund_sources
import data.domains.canteen.hidden
import data.domains.canteen.warning_messages


response := {
    "decision": final_decision,
    "limit": effective_limit,
    "hidden": hidden,
    "creditLimitEnabled": credit_limit_enabled,
    "accountSource": allowed_fund_sources,
    "warnings": warning_messages
    "errors": errors
}