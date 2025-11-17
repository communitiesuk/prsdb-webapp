package uk.gov.communities.prsdb.webapp.config.resolvers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.mvc.condition.RequestCondition
import uk.gov.communities.prsdb.webapp.services.FeatureFlagChecker

open class FeatureFlaggedRequestCondition(
    val flagName: String,
    val featureFlagChecker: FeatureFlagChecker,
) : RequestCondition<FeatureFlaggedRequestCondition> {
    override fun combine(other: FeatureFlaggedRequestCondition): FeatureFlaggedRequestCondition {
        return object : FeatureFlaggedRequestCondition(flagName, featureFlagChecker) {
            override fun getMatchingCondition(request: HttpServletRequest): FeatureFlaggedRequestCondition? {
                val thisEnabled = featureFlagChecker.isFeatureEnabled(this@FeatureFlaggedRequestCondition.flagName)
                val otherEnabled = featureFlagChecker.isFeatureEnabled(other.flagName)
                return if (thisEnabled && otherEnabled) this else null
            }
        }
    }

    override fun getMatchingCondition(request: HttpServletRequest): FeatureFlaggedRequestCondition? =
        if (featureFlagChecker.isFeatureEnabled(flagName)) {
            this
        } else {
            null
        }

    // Currently unused. Could be used to prioritise multiple matching conditions.
    override fun compareTo(
        other: FeatureFlaggedRequestCondition,
        request: HttpServletRequest,
    ): Int = 0
}
