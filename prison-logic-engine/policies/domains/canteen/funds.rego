package domains.canteen
import rego.v1


default allowed_fund_sources := ["spends"]

#
# FN account
#
allowed_fund_sources := [
    "cash",
    "spends"
] if {
    input.prisoner.isFN
}

#
# LG account
#
allowed_fund_sources := [
    "cash",
    "spends"
] if {
    input.prisoner.isLG
}

#
# Standard account
#
allowed_fund_sources := [
    "spends"
] if {
    not input.prisoner.isFN
    not input.prisoner.isLG
}