package uk.gov.communities.prsdb.webapp.config

import org.ff4j.core.FlippingStrategy
import uk.gov.communities.prsdb.webapp.config.factories.BooleanFlippingStrategyFactory
import uk.gov.communities.prsdb.webapp.config.factories.ReleaseDateFlippingStrategyFactory
import uk.gov.communities.prsdb.webapp.config.flipStrategies.CombinedFlipStrategy
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlipStrategyConfigModel

class FeatureFlipStrategyInitialiser(
    private val booleanFlipStrategyFactory: BooleanFlippingStrategyFactory,
    private val releaseDateFlipStrategyFactory: ReleaseDateFlippingStrategyFactory,
) {
    fun getFlipStrategyOrNull(strategy: FeatureFlipStrategyConfigModel): FlippingStrategy? {
        val strategyList = mutableListOf<FlippingStrategy>()

        releaseDateFlipStrategyFactory.getStrategyOrNull(strategy)?.let {
            strategyList.add(it)
        }
        booleanFlipStrategyFactory.getStrategyOrNull(strategy)?.let {
            strategyList.add(it)
        }

        return getCombinedFlipStrategyOrNull(strategyList)
    }

    private fun getCombinedFlipStrategyOrNull(strategyList: List<FlippingStrategy>): FlippingStrategy? {
        if (strategyList.isEmpty()) {
            return null
        }

        return CombinedFlipStrategy(strategyList)
    }
}
