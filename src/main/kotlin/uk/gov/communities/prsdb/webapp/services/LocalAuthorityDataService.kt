package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel

@Service
class LocalAuthorityDataService(
    val localAuthorityUserRepository: LocalAuthorityUserRepository,
    val localAuthorityUserInvitationRepository: LocalAuthorityUserInvitationRepository,
) {
    fun getLocalAuthorityUsersForLocalAuthority(localAuthority: LocalAuthority): List<LocalAuthorityUserDataModel> {
        val usersInThisLocalAuthority = localAuthorityUserRepository.findByLocalAuthority(localAuthority)
        return usersInThisLocalAuthority.map { LocalAuthorityUserDataModel(it.baseUser.name, it.isManager) }
    }

    fun getLocalAuthorityPendingUsersForLocalAuthority(localAuthority: LocalAuthority): List<LocalAuthorityUserDataModel> {
        val pendingUsers = localAuthorityUserInvitationRepository.findByLocalAuthority(localAuthority)
        return pendingUsers.map { LocalAuthorityUserDataModel(it.invitedEmailAddress!!, isManager = false, isPending = true) }
    }

    fun getLocalAuthorityForUser(subjectId: String): LocalAuthority? {
        val localAuthorityUser = localAuthorityUserRepository.findByBaseUser_Id(subjectId)
        return localAuthorityUser?.localAuthority
    }
}
