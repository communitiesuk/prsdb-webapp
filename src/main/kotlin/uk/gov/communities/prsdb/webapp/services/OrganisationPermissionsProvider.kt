package uk.gov.communities.prsdb.webapp.services

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_ADMIN

@PrsdbWebService
class OrganisationPermissionsProvider(
    // TODO PDJB-1828: Remove this dependency (and the then-empty constructor parentheses) when the
    //  MULTI_USER_ORGANISATIONS feature flag is removed
    private val featureFlagManager: FeatureFlagManager,
) {
    /**
     * Whether the current user holds the organisation admin role.
     *
     * This is a plain role check with no feature flag involvement, so it is safe for callers that need
     * the raw role regardless of MULTI_USER_ORGANISATIONS.
     */
    fun isCurrentUserOrgAdmin(): Boolean =
        SecurityContextHolder
            .getContext()
            .authentication
            ?.authorities
            ?.any { it.authority == ROLE_ORG_ADMIN }
            ?: false

    /**
     * Whether the current user may perform organisation admin actions (changing organisation details and
     * contacts, managing governing body members, and deleting the organisation).
     *
     * Only meaningful for an authenticated organisational landlord user: while the
     * MULTI_USER_ORGANISATIONS feature flag is off this returns true for any principal.
     */
    fun canCurrentUserPerformOrgAdminActions(): Boolean {
        // TODO PDJB-1828: Remove this block when the MULTI_USER_ORGANISATIONS feature flag is removed
        if (!featureFlagManager.checkFeature(MULTI_USER_ORGANISATIONS)) {
            return true
        }

        return isCurrentUserOrgAdmin()
    }
}
