package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository

@Service
class LocalAuthorityUserService(
    private val localAuthorityUserRepository: LocalAuthorityUserRepository,
) {
    fun getIsLocalAuthorityUser(baseUserId: String): Boolean = localAuthorityUserRepository.findByBaseUser_Id(baseUserId) != null
}
