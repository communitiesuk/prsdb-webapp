package uk.gov.communities.prsdb.webapp.config.flipStrategies

import org.ff4j.strategy.AbstractFlipStrategy

class BooleanFlipStrategy(
    private val enabledByStrategy: Boolean,
) : AbstractFlipStrategy() {
    override fun evaluate(
        featureName: String?,
        store: org.ff4j.core.FeatureStore?,
        executionContext: org.ff4j.core.FlippingExecutionContext?,
    ): Boolean {
        return enabledByStrategy
    }
}
