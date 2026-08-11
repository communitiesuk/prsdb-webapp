package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository

// TODO: PDJB-1274: Other email call sites still resolve recipients via Landlord.email - move them to this service
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
}
