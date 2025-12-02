package uk.gov.communities.prsdb.webapp.models.dataModels

data class FeatureReleaseConfigModel(
    val name: String,
    val enabled: Boolean,
    val strategyConfig: FeatureFlipStrategyConfigModel? = null,
)
