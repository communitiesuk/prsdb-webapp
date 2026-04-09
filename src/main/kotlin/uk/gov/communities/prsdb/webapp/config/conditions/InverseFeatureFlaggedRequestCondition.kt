package uk.gov.communities.prsdb.webapp.config.conditions

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.mvc.condition.RequestCondition
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager

open class InverseFeatureFlaggedRequestCondition(
    val flagName: String,
    val featureFlagManager: FeatureFlagManager,
) : RequestCondition<InverseFeatureFlaggedRequestCondition> {
    override fun combine(other: InverseFeatureFlaggedRequestCondition): InverseFeatureFlaggedRequestCondition =
        throw NotImplementedError("Combining feature flagged conditions has not been implemented.")

    override fun getMatchingCondition(request: HttpServletRequest): InverseFeatureFlaggedRequestCondition? =
        if (featureFlagManager.checkFeature(flagName)) {
            null
        } else {
            this
        }

    // Currently unused. Could be used to prioritise multiple matching conditions.
    override fun compareTo(
        other: InverseFeatureFlaggedRequestCondition,
        request: HttpServletRequest,
    ): Int = throw NotImplementedError("Comparing feature flagged conditions has not been implemented.")
}
