package uk.gov.communities.prsdb.webapp.config.factories

import org.ff4j.core.FlippingStrategy
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.config.flipStrategies.BooleanFlipStrategy
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlipStrategyConfigModel

@PrsdbWebComponent
class BooleanFlippingStrategyFactory : FlippingStrategyFactory {
    override fun getStrategyOrNull(strategy: FeatureFlipStrategyConfigModel): FlippingStrategy? {
        if (strategy.enabledByStrategy != null) {
            return BooleanFlipStrategy(strategy.enabledByStrategy)
        }
        return null
    }
}
