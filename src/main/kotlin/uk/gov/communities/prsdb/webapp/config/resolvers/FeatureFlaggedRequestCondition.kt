package uk.gov.communities.prsdb.webapp.config.resolvers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.mvc.condition.RequestCondition
import uk.gov.communities.prsdb.webapp.services.FeatureFlagChecker

class FeatureFlaggedRequestCondition(
    val flagName: String,
    val featureFlagChecker: FeatureFlagChecker,
) : RequestCondition<FeatureFlaggedRequestCondition> {
    override fun combine(other: FeatureFlaggedRequestCondition): FeatureFlaggedRequestCondition = other

    override fun getMatchingCondition(request: HttpServletRequest): FeatureFlaggedRequestCondition? =
        if (featureFlagChecker.isFeatureEnabled(flagName)) {
            this
        } else {
            null
        }

    override fun compareTo(
        other: FeatureFlaggedRequestCondition,
        request: HttpServletRequest,
    ): Int = 0
}
