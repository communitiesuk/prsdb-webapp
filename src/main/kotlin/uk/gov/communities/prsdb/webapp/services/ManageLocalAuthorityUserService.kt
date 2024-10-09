package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.LocalAuthorityUserDataModel

@Service
class ManageLocalAuthorityUserService(
    val localAuthorityUserRepository: LocalAuthorityUserRepository,
) {
    fun getLocalAuthorityUsersForLocalAuthority(localAuthorityId: Int): List<LocalAuthorityUserDataModel> {
        val usersInThisLocalAuthority = localAuthorityUserRepository.findByLocalAuthority_Id(localAuthorityId)
        return usersInThisLocalAuthority.map { LocalAuthorityUserDataModel(it.baseUser.name, it.isManager) }
    }
}
