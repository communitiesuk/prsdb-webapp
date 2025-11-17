package uk.gov.communities.prsdb.webapp.config.resolvers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.mvc.condition.RequestCondition
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager

open class FeatureFlaggedRequestCondition(
    val flagName: String,
    val featureFlagManager: FeatureFlagManager,
) : RequestCondition<FeatureFlaggedRequestCondition> {
    override fun combine(other: FeatureFlaggedRequestCondition): FeatureFlaggedRequestCondition {
        return object : FeatureFlaggedRequestCondition(flagName, featureFlagManager) {
            override fun getMatchingCondition(request: HttpServletRequest): FeatureFlaggedRequestCondition? {
                val thisEnabled = featureFlagManager.checkFeature(this@FeatureFlaggedRequestCondition.flagName)
                val otherEnabled = featureFlagManager.checkFeature(other.flagName)
                return if (thisEnabled && otherEnabled) this else null
            }
        }
    }

    override fun getMatchingCondition(request: HttpServletRequest): FeatureFlaggedRequestCondition? =
        if (featureFlagManager.checkFeature(flagName)) {
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
