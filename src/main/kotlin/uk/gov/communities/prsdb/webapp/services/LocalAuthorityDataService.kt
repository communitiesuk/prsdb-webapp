package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel

@Service
class LocalAuthorityDataService(
    val localAuthorityUserRepository: LocalAuthorityUserRepository,
) {
    fun getLocalAuthorityUsersForLocalAuthority(localAuthority: LocalAuthority): List<LocalAuthorityUserDataModel> {
        val usersInThisLocalAuthority = localAuthorityUserRepository.findByLocalAuthority(localAuthority)
        return usersInThisLocalAuthority.map { LocalAuthorityUserDataModel(it.baseUser.name, it.isManager) }
    }

    fun getLocalAuthorityForUser(subjectId: String): LocalAuthority? {
        val localAuthorityUser = localAuthorityUserRepository.findByBaseUser_Id(subjectId)
        return localAuthorityUser?.localAuthority
    }
}
