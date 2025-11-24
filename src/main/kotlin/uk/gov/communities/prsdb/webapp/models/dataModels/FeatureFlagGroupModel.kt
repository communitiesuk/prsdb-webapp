package uk.gov.communities.prsdb.webapp.models.dataModels

import java.time.LocalDate

data class FeatureFlagGroupModel(
    val name: String,
    val enabled: Boolean,
    val releaseDate: LocalDate? = null,
)
