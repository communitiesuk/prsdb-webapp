package uk.gov.communities.prsdb.webapp.config

import org.ff4j.core.FlippingStrategy
import org.ff4j.strategy.time.ReleaseDateFlipStrategy
import uk.gov.communities.prsdb.webapp.config.flipStrategies.BooleanFlipStrategy
import uk.gov.communities.prsdb.webapp.config.flipStrategies.CombinedFlipStrategy
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlipStrategyConfigModel

class FeatureFlipStrategyInitialiser {
    fun getFlipStrategyOrNull(strategy: FeatureFlipStrategyConfigModel): FlippingStrategy? {
        val strategyList = mutableListOf<FlippingStrategy>()

        getReleaseDateStrategyOrNull(strategy)?.let {
            strategyList.add(it)
        }
        getBooleanStrategyOrNull(strategy)?.let {
            strategyList.add(it)
        }

        return getCombinedFlipStrategyOrNull(strategyList)
    }

    private fun getReleaseDateStrategyOrNull(strategy: FeatureFlipStrategyConfigModel): ReleaseDateFlipStrategy? {
        if (strategy.releaseDate != null) {
            // Initialise feature flip strategy based on release date
            return ReleaseDateFlipStrategy(DateTimeHelper.getJavaDateFromLocalDate(strategy.releaseDate))
        }
        return null
    }

    private fun getBooleanStrategyOrNull(strategy: FeatureFlipStrategyConfigModel): FlippingStrategy? {
        if (strategy.enabledByStrategy != null) {
            return BooleanFlipStrategy(strategy.enabledByStrategy)
        }
        return null
    }

    private fun getCombinedFlipStrategyOrNull(strategyList: List<FlippingStrategy>): FlippingStrategy? {
        if (strategyList.isEmpty()) {
            return null
        }

        return CombinedFlipStrategy(strategyList)
    }
}
