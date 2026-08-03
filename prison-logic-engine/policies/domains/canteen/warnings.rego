package domains.canteen
import rego.v1

warning_messages contains
"Proposed balance exceeds phone credit limit" if {

    credit_limit_exceeded
}
