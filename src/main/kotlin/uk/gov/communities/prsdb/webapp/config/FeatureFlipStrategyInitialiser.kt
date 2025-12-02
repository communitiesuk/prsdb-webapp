package uk.gov.communities.prsdb.webapp.config

import org.ff4j.core.FlippingStrategy
import org.ff4j.strategy.time.ReleaseDateFlipStrategy
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlipStrategyConfigModel

class FeatureFlipStrategyInitialiser {
    fun getFlipStrategyOrNull(strategy: FeatureFlipStrategyConfigModel): FlippingStrategy? {
        val strategyList = mutableListOf<FlippingStrategy>()

        getReleaseDateStrategyOrNull(strategy)?.let {
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

    private fun getCombinedFlipStrategyOrNull(strategyList: List<FlippingStrategy>): FlippingStrategy? {
        if (strategyList.isEmpty()) {
            return null
        }
        if (strategyList.size == 1) {
            return strategyList[0]
        }

        // TODO PRSD-1647 - can we return a combined strategy here?
        return strategyList[0]
    }
}
