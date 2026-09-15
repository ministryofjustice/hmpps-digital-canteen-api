package domains.canteen

import rego.v1

default effective_limit := 0

default credit_limit_enabled := false

default credit_limit_exceeded := false

credit_limit_max := 5000

prison_limit := limit if {
    prison := input.prisoner.prisonId

    iep := input.incentives.iepCode

    limit := data.prisons[prison].products["BT_PIN_Phone"][iep].limit
}

#
# Credit limits only apply to BT PIN Phone
#
credit_limit_enabled := true if {
    input.productId == "BT_PIN_Phone"

    not input.prisoner.isFN

    not input.prisoner.isLG
}

effective_limit := limit if {
    input.productId == "BT_PIN_Phone"

    credit_limit_enabled

    limit := min([
        prison_limit,
        credit_limit_max
    ])
}

proposed_balance := balance if {
    input.productId == "BT_PIN_Phone"

    current := object.get(input, "currentBalance", 0)

    requested := object.get(input, "creditRequested", 0)

    balance := current + requested
}

credit_limit_exceeded if {
    input.productId == "BT_PIN_Phone"

    credit_limit_enabled

    proposed_balance > effective_limit
}