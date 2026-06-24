package ecommerce.visibility.internal

default final_hide := false

default final_labels := []

default final_warnings := []

# Rule 1: ANY prisoner with active adjudications sees NO products
final_hide if {
	input.prisoner.hasActiveAdjudications
}

# Rule 2: Youth offenders with NO adjudications cannot see vape/tobacco/alcohol
final_hide if {
	not input.prisoner.hasActiveAdjudications
	input.prisoner.prisoner.youthOffender
	kw := input.product.product._keywords[_]
	kw in ["vape", "tobacco", "alcohol"]
}

decision := [{
	"productId": input.product.code,
	"hide": final_hide,
	"show_warnings": final_warnings,
	"labels": final_labels,
}]
