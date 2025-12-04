package uk.gov.communities.prsdb.webapp.config

import org.ff4j.core.FlippingStrategy
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.config.factories.FlippingStrategyFactory
import uk.gov.communities.prsdb.webapp.config.flipStrategies.CombinedFlipStrategy
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlipStrategyConfigModel

@Component
class FeatureFlipStrategyInitialiser(
    private val flippingStrategyFactories: List<FlippingStrategyFactory> = emptyList(),
) {
    fun getFlipStrategyOrNull(strategy: FeatureFlipStrategyConfigModel): FlippingStrategy? {
        val strategyList = mutableListOf<FlippingStrategy>()

        flippingStrategyFactories.forEach { factory ->
            factory.getStrategyOrNull(strategy)?.let { strategyList.add(it) }
        }

        if (strategyList.isEmpty()) {
            return null
        }

        return CombinedFlipStrategy(strategyList)
    }
}
