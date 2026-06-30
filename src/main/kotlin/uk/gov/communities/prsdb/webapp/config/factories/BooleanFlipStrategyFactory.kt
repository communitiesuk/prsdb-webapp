package uk.gov.communities.prsdb.webapp.config.factories

import org.ff4j.core.FlippingStrategy
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.config.flipStrategies.BooleanFlipStrategy
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlipStrategyConfigModel

@Component
class BooleanFlipStrategyFactory : FlippingStrategyFactory {
    override fun getStrategyOrNull(strategy: FeatureFlipStrategyConfigModel): FlippingStrategy? {
        if (strategy.enabledByStrategy != null) {
            return BooleanFlipStrategy(strategy.enabledByStrategy)
        }
        return null
    }
}
