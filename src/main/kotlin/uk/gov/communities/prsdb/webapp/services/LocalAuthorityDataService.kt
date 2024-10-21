package uk.gov.communities.prsdb.webapp.services

import org.springframework.data.domain.PageRequest
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
    fun getLocalAuthorityUsersForLocalAuthority(
        localAuthority: LocalAuthority,
        pageRequest: PageRequest,
    ): List<LocalAuthorityUserDataModel> {
        val usersInThisLocalAuthority = localAuthorityUserRepository.findByLocalAuthority(localAuthority, pageRequest)
        return usersInThisLocalAuthority.map { LocalAuthorityUserDataModel(it.baseUser.name, it.isManager) }
    }

    fun getLocalAuthorityPendingUsersForLocalAuthority(
        localAuthority: LocalAuthority,
        pageRequest: PageRequest,
    ): List<LocalAuthorityUserDataModel> {
        val pendingUsers = localAuthorityInvitationRepository.findByInvitingAuthority(localAuthority, pageRequest)
        return pendingUsers.map { LocalAuthorityUserDataModel(it.invitedEmail, isManager = false, isPending = true) }
    }

    fun getLocalAuthorityForUser(subjectId: String): LocalAuthority? {
        val localAuthorityUser = localAuthorityUserRepository.findByBaseUser_Id(subjectId)
        return localAuthorityUser?.localAuthority
    }

    fun countActiveLocalAuthorityUsersForLocalAuthority(localAuthority: LocalAuthority): Long =
        localAuthorityUserRepository.countByLocalAuthority(localAuthority)

    fun countPendingLocalAuthorityUsersForLocalAuthority(localAuthority: LocalAuthority): Long =
        localAuthorityInvitationRepository.countByInvitingAuthority(localAuthority)
}
