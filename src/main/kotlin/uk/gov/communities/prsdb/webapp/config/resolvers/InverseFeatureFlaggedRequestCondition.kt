package uk.gov.communities.prsdb.webapp.config.resolvers

import jakarta.servlet.http.HttpServletRequest
import uk.gov.communities.prsdb.webapp.services.FeatureFlagChecker

class InverseFeatureFlaggedRequestCondition(
    flagName: String,
    featureFlagChecker: FeatureFlagChecker,
) : FeatureFlaggedRequestCondition(flagName, featureFlagChecker) {
    override fun getMatchingCondition(request: HttpServletRequest): FeatureFlaggedRequestCondition? =
        if (featureFlagChecker.isFeatureEnabled(flagName)) {
            null
        } else {
            this
        }
}
