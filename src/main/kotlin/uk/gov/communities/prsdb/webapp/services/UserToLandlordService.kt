package uk.gov.communities.prsdb.webapp.services

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationLandlordUserRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException

/**
 * Given a user, find the landlord they're associated with
 * Normally assumed to be the current landlord you're logged in as
 */
@PrsdbWebService
class UserToLandlordService(
    private val individualLandlordRepository: IndividualLandlordRepository,
    private val organisationLandlordUserRepository: OrganisationLandlordUserRepository,
) {
    fun getCurrentLandlordForUser(): Landlord {
        // TODO: PDJB-1477: Improve this method with caching
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        return getLandlordForBaseUserId(baseUserId)
    }

    fun getCurrentLandlordForUserOrNull(): Landlord? {
        // TODO: PDJB-1477: Improve this method with caching
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        return getLandlordForBaseUserIdOrNull(baseUserId)
    }

    fun doesCurrentUserHaveLandlord(): Boolean = getCurrentLandlordForUserOrNull() != null

    /**
     * Calls getCurrentLandlordForUser and discards result.
     * Use for checking that the user has a landlord and expect a ResponseStatusException(BAD_REQUEST) if they do not.
     */
    fun throwIfCurrentUserDoesNotHaveALandlord() {
        getCurrentLandlordForUser()
    }

    /**
     * Be careful about using this, at some point we may need to allow for one user to be in control of multiple landlords
     * Where possible use getCurrentLandlordForUser()
     */
    fun getLandlordForBaseUserId(baseUserId: String): Landlord =
        getLandlordForBaseUserIdOrNull(baseUserId)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No landlord was found for user with baseUserId $baseUserId")

    /**
     * Be careful about using this, at some point we may need to allow for one user to be in control of multiple landlords
     * Where possible use getCurrentLandlordForUser()
     */
    fun getLandlordForBaseUserIdOrNull(baseUserId: String): Landlord? {
        val landlords =
            listOfNotNull(individualLandlordRepository.findByBaseUser_Id(baseUserId)) +
                organisationLandlordUserRepository.findByBaseUser_Id(baseUserId).map { it.organisationLandlord }

        if (landlords.size > 1) {
            throw PrsdbWebException("Multiple landlords were found for user with baseUserId $baseUserId")
        }

        return landlords.singleOrNull()
    }
}
