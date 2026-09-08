package domains.rules

import rego.v1

condition_matches(rule) if {
    not rule.conditions
}

#
# Age condition
#
condition_matches(rule) if {

    age := object.get(
        rule.conditions.prisoner,
        "age",
        null
    )

    age != null

    age.operator == "<"

    input.prisoner.age < age.value
}

#
# Arson
#
condition_matches(rule) if {

    object.get(
        rule.conditions.prisoner,
        "arsonCharge",
        false
    )

    input.prisoner.arsonCharge == true
}

#
# Religious
#

condition_matches(rule) if {

    object.get(
        rule.conditions.prisoner,
        "religiousAffiliation",
        false
    )

    input.prisoner.religiousAffiliation == true
}


#
# Foreign National
#
condition_matches(rule) if {

    object.get(
        rule.conditions.prisoner,
        "isFN",
        false
    )

    input.prisoner.isFN == true
}

#
# LG
#
condition_matches(rule) if {

    object.get(
        rule.conditions.prisoner,
        "isLG",
        false
    )

    input.prisoner.isLG == true
}

#
# Standard BT Account
#
condition_matches(rule) if {

    rule.ruleId == "BT_STANDARD_LIMIT"

    not input.prisoner.isFN

    not input.prisoner.isLG
}

condition_matches(rule) if {

    rule.ruleId == "R004"

    input.prisoner.arsonCharge == true

    input.prisoner.religiousAffiliation == true
}

condition_matches(rule) if {

    rule.ruleId == "R005"

    input.prisoner.arsonCharge == true
}