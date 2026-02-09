package uk.gov.communities.prsdb.webapp.testHelpers

import org.ff4j.core.FlippingStrategy
import org.ff4j.strategy.time.ReleaseDateFlipStrategy
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.config.flipStrategies.BooleanFlipStrategy
import uk.gov.communities.prsdb.webapp.config.flipStrategies.CombinedFlipStrategy
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseConfigModel
import java.time.LocalDate

class FeatureFlagConfigUpdater(
    private val featureFlagManager: FeatureFlagManager,
) {
    companion object {
        fun resetToConfiguration(
            featureFlagManager: FeatureFlagManager,
            featureFlags: List<FeatureFlagConfigModel>,
            featureReleases: List<FeatureReleaseConfigModel>,
        ) {
            // Access the featureStore from the parent FF4j class
            featureFlagManager.featureStore.clear()
            featureFlagManager.initializeFeatureFlags(featureFlags)
            featureFlagManager.initialiseFeatureReleases(featureReleases)
        }
    }

    fun updateFeatureReleaseDate(
        featureName: String,
        newReleaseDate: LocalDate,
    ) {
        val feature =
            featureFlagManager.features.get(featureName)
                ?: throw IllegalArgumentException("Feature flag '$featureName' not found")

        if (feature.flippingStrategy is CombinedFlipStrategy) {
            val combinedStrategy = feature.flippingStrategy as CombinedFlipStrategy
            (combinedStrategy.strategyList.find { it is ReleaseDateFlipStrategy })
                ?.let { updateFlippingStrategyReleaseDate(it, newReleaseDate) }
                ?: throw IllegalArgumentException(
                    "Feature flag '$featureName' does not have a ReleaseDateStrategy in its CombinedFlipStrategy",
                )
        } else if (feature.flippingStrategy is ReleaseDateFlipStrategy) {
            updateFlippingStrategyReleaseDate(feature.flippingStrategy, newReleaseDate)
        } else {
            throw IllegalArgumentException("Feature flag '$featureName' does not have a ReleaseDateFlipStrategy")
        }
    }

    private fun updateFlippingStrategyReleaseDate(
        flippingStrategy: FlippingStrategy,
        newReleaseDate: LocalDate,
    ) {
        if (flippingStrategy !is ReleaseDateFlipStrategy) {
            throw IllegalArgumentException("Provided flipping strategy is not a ReleaseDateFlipStrategy")
        }
        flippingStrategy.setReleaseDate(DateTimeHelper.getJavaDateFromLocalDate(newReleaseDate))
    }

    fun updateFeatureEnabledByStrategy(
        featureName: String,
        enabledByStrategy: Boolean,
    ) {
        val feature =
            featureFlagManager.features.get(featureName)
                ?: throw IllegalArgumentException("Feature flag '$featureName' not found")

        if (feature.flippingStrategy is CombinedFlipStrategy) {
            val combinedStrategy = feature.flippingStrategy as CombinedFlipStrategy
            (combinedStrategy.strategyList.find { it is BooleanFlipStrategy })
                ?.let { updateFlippingStrategyEnabledByStrategy(it, enabledByStrategy) }
                ?: throw IllegalArgumentException(
                    "Feature flag '$featureName' does not have a BooleanFlipStrategy in its CombinedFlipStrategy",
                )
        } else if (feature.flippingStrategy is BooleanFlipStrategy) {
            updateFlippingStrategyEnabledByStrategy(feature.flippingStrategy, enabledByStrategy)
        } else {
            throw IllegalArgumentException("Feature flag '$featureName' does not have a BooleanFlipStrategy")
        }
    }

    private fun updateFlippingStrategyEnabledByStrategy(
        flippingStrategy: FlippingStrategy,
        enabledByStrategy: Boolean,
    ) {
        if (flippingStrategy !is BooleanFlipStrategy) {
            throw IllegalArgumentException("Provided flipping strategy is not a BooleanFlipStrategy")
        }
        // Using reflection to update the private property 'enabledByStrategy'
        ReflectionTestUtils.setField(flippingStrategy, "enabledByStrategy", enabledByStrategy)
    }

    fun updateReleaseReleaseDate(
        releaseName: String,
        newReleaseDate: LocalDate,
    ) {
        val featuresInRelease = featureFlagManager.getFeaturesByGroup(releaseName)

        featuresInRelease.forEach { (_, feature) ->
            updateFeatureReleaseDate(feature.uid, newReleaseDate)
        }
    }

    fun updateReleaseEnabledByStrategy(
        releaseName: String,
        enabledByStrategy: Boolean,
    ) {
        val featuresInRelease = featureFlagManager.getFeaturesByGroup(releaseName)

        featuresInRelease.forEach { (_, feature) ->
            updateFeatureEnabledByStrategy(feature.uid, enabledByStrategy)
        }
    }

    fun reinitialiseFeatures(
        featureFlags: List<FeatureFlagConfigModel>,
        featureReleases: List<FeatureReleaseConfigModel> = emptyList(),
    ) {
        resetToConfiguration(featureFlagManager, featureFlags, featureReleases)
    }
}
