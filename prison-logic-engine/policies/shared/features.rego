package shared.features
import rego.v1

enabled(feature) if {
    feature in data.prisons[input.prisoner.prisonId].features
}