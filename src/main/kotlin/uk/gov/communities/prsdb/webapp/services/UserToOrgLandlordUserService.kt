package uk.gov.communities.prsdb.webapp.services

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository

/**
 * Given a user, find the organisational landlord team member record they're associated with
 * Normally assumed to be the current user you're logged in as
 */
@PrsdbWebService
class UserToOrgLandlordUserService(
    private val organisationalLandlordUserRepository: OrganisationalLandlordUserRepository,
) {
    fun getOrganisationalLandlordUserForCurrentUser(): OrganisationalLandlordUser? {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        return organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId).singleOrNull()
    }
}
