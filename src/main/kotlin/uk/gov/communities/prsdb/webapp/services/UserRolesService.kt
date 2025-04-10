package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_USER
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
            roles.add(ROLE_LANDLORD)
        }

        val matchingLocalAuthorityUser = localAuthorityUserRepository.findByBaseUser_Id(subjectId)
        if (matchingLocalAuthorityUser != null) {
            if (matchingLocalAuthorityUser.isManager) {
                roles.add(ROLE_LA_ADMIN)
            }
            roles.add(ROLE_LA_USER)
        }

        return roles
    }

    fun getHasLandlordUserRole(subjectId: String): Boolean {
        val roles = getRolesForSubjectId(subjectId)
        return roles.contains(ROLE_LANDLORD)
    }
}
