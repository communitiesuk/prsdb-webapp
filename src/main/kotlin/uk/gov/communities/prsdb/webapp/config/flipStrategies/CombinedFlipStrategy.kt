package uk.gov.communities.prsdb.webapp.config.flipStrategies

import org.ff4j.core.FeatureStore
import org.ff4j.core.FlippingExecutionContext
import org.ff4j.core.FlippingStrategy
import org.ff4j.strategy.AbstractFlipStrategy

class CombinedFlipStrategy(
    private val strategyList: List<FlippingStrategy>,
) : AbstractFlipStrategy() {
    override fun evaluate(
        featureName: String?,
        store: FeatureStore?,
        executionContext: FlippingExecutionContext?,
    ): Boolean {
        for (strategy in strategyList) {
            if (!strategy.evaluate(featureName, store, executionContext)) {
                return false
            }
        }
        return true
    }
}
