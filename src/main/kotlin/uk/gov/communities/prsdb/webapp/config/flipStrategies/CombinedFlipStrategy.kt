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
        var result: Boolean
        for (strategy in strategyList) {
            result = true && strategy.evaluate(featureName, store, executionContext)
            if (!result) {
                return false
            }
        }
        return true
    }
}
