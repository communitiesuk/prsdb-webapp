package uk.gov.communities.prsdb.webapp.services

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException

@PrsdbWebService
class CurrentEmailService(
    private val individualLandlordRepository: IndividualLandlordRepository,
    private val organisationalLandlordUserRepository: OrganisationalLandlordUserRepository,
) {
    fun getCurrentEmail(): String = getCurrentUserDetails().email

    fun getCurrentName(): String = getCurrentUserDetails().name

    fun getCurrentUserDetails(): CurrentUserDetails {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val individualLandlord = individualLandlordRepository.findByBaseUser_Id(baseUserId)
        val organisationalUsers = organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId)

        val matchingUsers =
            listOfNotNull(
                individualLandlord?.let { CurrentUserDetails(name = it.name, email = it.email) },
            ) + organisationalUsers.map { CurrentUserDetails(name = it.name, email = it.email) }

        if (matchingUsers.size > 1) {
            throw PrsdbWebException("Multiple landlords were found for user with baseUserId $baseUserId")
        }

        return matchingUsers.singleOrNull()
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No landlord was found for user with baseUserId $baseUserId",
            )
    }

    data class CurrentUserDetails(
        val name: String,
        val email: String,
    )
}
