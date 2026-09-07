package uk.gov.communities.prsdb.webapp.models.dataModels

import java.io.Serializable

data class FeatureFlagOverrides(
    val flags: Map<String, Boolean> = emptyMap(),
    val releases: Map<String, Boolean> = emptyMap(),
) : Serializable {
    fun isEmpty() = flags.isEmpty() && releases.isEmpty()

    fun isNotEmpty() = !isEmpty()
}
