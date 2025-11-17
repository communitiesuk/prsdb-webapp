package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Component
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager

@Component
class FeatureFlagChecker(
    private val featureFlagManager: FeatureFlagManager,
) {
    fun isFeatureEnabled(flagName: String): Boolean = featureFlagManager.checkFeature(flagName)
}
