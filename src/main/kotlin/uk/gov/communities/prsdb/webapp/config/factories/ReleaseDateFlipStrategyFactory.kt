package uk.gov.communities.prsdb.webapp.config.factories

import org.ff4j.core.FlippingStrategy
import org.ff4j.strategy.time.ReleaseDateFlipStrategy
import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlipStrategyConfigModel

@Component
class ReleaseDateFlipStrategyFactory : FlippingStrategyFactory {
    override fun getStrategyOrNull(strategy: FeatureFlipStrategyConfigModel): FlippingStrategy? {
        if (strategy.releaseDate != null) {
            // Initialise feature flip strategy based on release date
            return ReleaseDateFlipStrategy(DateTimeHelper.Companion.getJavaDateFromLocalDate(strategy.releaseDate))
        }
        return null
    }
}
