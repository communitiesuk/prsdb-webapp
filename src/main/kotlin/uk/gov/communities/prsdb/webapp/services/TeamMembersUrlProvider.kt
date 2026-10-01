package uk.gov.communities.prsdb.webapp.services

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository

@PrsdbWebService
class TeamMembersUrlProvider(
    private val featureFlagManager: FeatureFlagManager,
    private val organisationalLandlordUserRepository: OrganisationalLandlordUserRepository,
) {
    fun getTeamMembersUrlForCurrentUser(): String? {
        if (!featureFlagManager.checkFeature(MULTI_USER_ORGANISATIONS)) {
            return null
        }

        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        if (authentication.authorities.none { it.authority == ROLE_LANDLORD }) {
            return null
        }

        return if (organisationalLandlordUserRepository.existsByBaseUser_Id(authentication.name)) TEAM_MEMBERS_ROUTE else null
    }
}
