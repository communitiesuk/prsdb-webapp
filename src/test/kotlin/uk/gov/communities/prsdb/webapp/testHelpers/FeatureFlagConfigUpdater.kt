package uk.gov.communities.prsdb.webapp.testHelpers

import org.ff4j.core.FlippingStrategy
import org.ff4j.strategy.time.ReleaseDateFlipStrategy
import uk.gov.communities.prsdb.webapp.config.flipStrategies.CombinedFlipStrategy
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.LocalDate

class FeatureFlagConfigUpdater(
    private val featureFlagManager: FeatureFlagManager,
) {
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
}
