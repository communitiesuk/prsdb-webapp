package uk.gov.communities.prsdb.webapp.services

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_ADMIN
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE

@PrsdbWebService
class ManageTeamMembersUrlProvider(
    private val featureFlagManager: FeatureFlagManager,
) {
    fun getManageTeamMembersUrlForCurrentUser(): String? {
        // TODO PDJB-1828: Remove this check when the MULTI_USER_ORGANISATIONS flag is removed
        if (!featureFlagManager.checkFeature(MULTI_USER_ORGANISATIONS)) {
            return null
        }

        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        if (authentication.authorities.none { it.authority == ROLE_ORG_ADMIN }) {
            return null
        }

        return TEAM_MEMBERS_ROUTE
    }
}
