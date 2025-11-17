package uk.gov.communities.prsdb.webapp.config.resolvers

import jakarta.servlet.http.HttpServletRequest
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager

class InverseFeatureFlaggedRequestCondition(
    flagName: String,
    featureFlagManager: FeatureFlagManager,
) : FeatureFlaggedRequestCondition(flagName, featureFlagManager) {
    override fun getMatchingCondition(request: HttpServletRequest): FeatureFlaggedRequestCondition? =
        if (featureFlagManager.checkFeature(flagName)) {
            null
        } else {
            this
        }
}
