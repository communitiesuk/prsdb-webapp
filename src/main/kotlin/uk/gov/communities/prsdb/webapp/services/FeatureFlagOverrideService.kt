package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.context.request.RequestContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.FEATURE_FLAG_OVERRIDES
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagOverrides

@PrsdbWebService
class FeatureFlagOverrideService(
    private val session: HttpSession,
    @Value("\${features.overrides-enabled:false}") private val overridesEnabled: Boolean,
) {
    fun getOverrides(): FeatureFlagOverrides {
        if (!isAvailable()) return FeatureFlagOverrides()
        return session.getAttribute(FEATURE_FLAG_OVERRIDES) as? FeatureFlagOverrides ?: FeatureFlagOverrides()
    }

    fun setOverrides(overrides: FeatureFlagOverrides) {
        if (!isAvailable()) return
        session.setAttribute(FEATURE_FLAG_OVERRIDES, overrides)
    }

    fun clearOverrides() = setOverrides(FeatureFlagOverrides())

    fun hasActiveOverrides() = getOverrides().isNotEmpty()

    // The session is a scoped proxy that throws outside a request, and feature flags are checked during startup
    private fun isAvailable() = overridesEnabled && RequestContextHolder.getRequestAttributes() != null
}
