package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel

@Service
class LocalAuthorityDataService(
    val localAuthorityUserRepository: LocalAuthorityUserRepository,
    val localAuthorityInvitationRepository: LocalAuthorityInvitationRepository,
) {
    fun getLocalAuthorityUsersForLocalAuthority(localAuthority: LocalAuthority): List<LocalAuthorityUserDataModel> {
        val usersInThisLocalAuthority = localAuthorityUserRepository.findByLocalAuthorityOrderByBaseUser_Name(localAuthority)
        return usersInThisLocalAuthority.map { LocalAuthorityUserDataModel(it.baseUser.name, it.isManager) }
    }

    fun getLocalAuthorityPendingUsersForLocalAuthority(localAuthority: LocalAuthority): List<LocalAuthorityUserDataModel> {
        val pendingUsers = localAuthorityInvitationRepository.findByInvitingAuthorityOrderByInvitedEmail(localAuthority)
        return pendingUsers.map { LocalAuthorityUserDataModel(it.invitedEmail, isManager = false, isPending = true) }
    }

    fun getLocalAuthorityForUser(subjectId: String): LocalAuthority? {
        val localAuthorityUser = localAuthorityUserRepository.findByBaseUser_Id(subjectId)
        return localAuthorityUser?.localAuthority
    }
}
