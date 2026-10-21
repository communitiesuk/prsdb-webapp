package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository

@PrsdbTaskService
class LandlordUserEmailService(
    private val individualLandlordRepository: IndividualLandlordRepository,
    private val organisationalLandlordUserRepository: OrganisationalLandlordUserRepository,
) {
    fun getEmailsByBaseUserId(baseUserIds: Collection<String>): Map<String, String> {
        if (baseUserIds.isEmpty()) return emptyMap()

        val individualLandlordEmails =
            individualLandlordRepository
                .findByBaseUser_IdIn(baseUserIds)
                .associate { it.baseUser.id to it.email }

        val organisationalLandlordUserEmails =
            organisationalLandlordUserRepository
                .findByBaseUser_IdIn(baseUserIds)
                .associate { it.baseUser.id to it.email }

        return individualLandlordEmails + organisationalLandlordUserEmails
    }

    fun getUserDetailsByBaseUserId(baseUserId: String): LandlordUserDetails {
        val individualLandlord = individualLandlordRepository.findByBaseUser_Id(baseUserId)
        val organisationalUsers = organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId)

        val matchingUsers =
            listOfNotNull(
                individualLandlord?.let { LandlordUserDetails(name = it.name, email = it.email) },
            ) + organisationalUsers.map { LandlordUserDetails(name = it.name, email = it.email) }

        if (matchingUsers.size > 1) {
            throw IllegalStateException("Multiple landlord users found for base user id $baseUserId")
        }

        return matchingUsers.singleOrNull()
            ?: throw IllegalStateException("No landlord user found for base user id $baseUserId")
    }
}

data class LandlordUserDetails(
    val name: String,
    val email: String,
)
