package uk.gov.communities.prsdb.webapp.config.factories

import org.ff4j.core.FlippingStrategy
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlipStrategyConfigModel

interface FlippingStrategyFactory {
    fun getStrategyOrNull(strategy: FeatureFlipStrategyConfigModel): FlippingStrategy?
}
