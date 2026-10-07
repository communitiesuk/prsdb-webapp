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
    fun isCurrentUserOrgAdmin(): Boolean =
        SecurityContextHolder
            .getContext()
            .authentication
            ?.authorities
            ?.any { it.authority == ROLE_ORG_ADMIN }
            ?: false

    fun canCurrentUserPerformOrgAdminActions(): Boolean {
        // TODO PDJB-1828: Remove this block when the MULTI_USER_ORGANISATIONS feature flag is removed
        if (!featureFlagManager.checkFeature(MULTI_USER_ORGANISATIONS)) {
            return true
        }

        return isCurrentUserOrgAdmin()
    }
}
