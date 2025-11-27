package uk.gov.communities.prsdb.webapp.models.dataModels

import java.time.LocalDate

data class FeatureFlagModel(
    val name: String,
    val enabled: Boolean,
    val expiryDate: LocalDate,
    val release: String? = null,
)
