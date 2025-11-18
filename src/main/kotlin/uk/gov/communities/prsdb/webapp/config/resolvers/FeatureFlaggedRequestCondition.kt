package uk.gov.communities.prsdb.webapp.config.resolvers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.mvc.condition.RequestCondition
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager

open class FeatureFlaggedRequestCondition(
    val flagName: String,
    val featureFlagManager: FeatureFlagManager,
) : RequestCondition<FeatureFlaggedRequestCondition> {
    // Currently unused. Could be used to combine different conditions e.g. class level and method level conditions.
    override fun combine(other: FeatureFlaggedRequestCondition): FeatureFlaggedRequestCondition =
        throw NotImplementedError("Combining feature flagged conditions has not been implemented.")

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
    ): Int = throw NotImplementedError("Comparing feature flagged conditions has not been implemented.")
}
