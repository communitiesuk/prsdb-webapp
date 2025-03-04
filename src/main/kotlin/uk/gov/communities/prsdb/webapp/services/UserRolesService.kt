package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository

@Service
class UserRolesService(
    val landlordRepository: LandlordRepository,
    val localAuthorityUserRepository: LocalAuthorityUserRepository,
) {
    fun getRolesForSubjectId(subjectId: String): List<String> {
        val roles = mutableListOf<String>()

        val matchingLandlordUser = landlordRepository.findByBaseUser_Id(subjectId)
        if (matchingLandlordUser != null) {
            roles.add("ROLE_LANDLORD")
        }

        val matchingLocalAuthorityUser = localAuthorityUserRepository.findByBaseUser_Id(subjectId)
        if (matchingLocalAuthorityUser != null) {
            if (matchingLocalAuthorityUser.isManager) {
                roles.add("ROLE_LA_ADMIN")
            }
            roles.add("ROLE_LA_USER")
        }

        return roles
    }
}
