package ecommerce.visibility.internal

default final_deny := false

default has_active_adjudications := false
has_active_adjudications if input.prisoner.hasActiveAdjudications

default spend_amount_lower := false
spend_amount_lower if input.prisoner.prisonerBalance.spends < input.creditRequested

# Rule 1: Any prisoner with active adjudications cannot purchase pin credit
final_deny if has_active_adjudications

# Rule 2: Deny if requested credit exceeds available balance
final_deny if spend_amount_lower

final_warnings := [msg |
    some check in warning_checks
    check.condition
    msg := check.message
]

warning_checks := [
    {
        "condition": has_active_adjudications,
        "message": "Active adjudications - purchase denied",
    },
    {
        "condition": spend_amount_lower,
        "message": "Spends total is lower than requested credit",
    },
]

decision := [{
    "deny_purchase": final_deny,
    "show_warnings": final_warnings,
}]