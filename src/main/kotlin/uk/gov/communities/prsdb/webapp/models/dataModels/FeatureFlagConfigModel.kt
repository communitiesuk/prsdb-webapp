package uk.gov.communities.prsdb.webapp.models.dataModels

import java.time.LocalDate

data class FeatureFlagConfigModel(
    val name: String,
    val enabled: Boolean,
    val expiryDate: LocalDate,
    val release: String? = null,
    val strategyConfig: FeatureFlipStrategyConfigModel? = null,
)
