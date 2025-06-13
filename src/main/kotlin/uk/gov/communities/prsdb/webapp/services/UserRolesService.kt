package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.WebService
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SystemOperatorRepository

@WebService
class UserRolesService(
    val landlordRepository: LandlordRepository,
    val localAuthorityUserRepository: LocalAuthorityUserRepository,
    val systemOperatorRepository: SystemOperatorRepository,
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

        val matchingSystemOperator = systemOperatorRepository.findByBaseUser_Id(subjectId)
        if (matchingSystemOperator != null) {
            roles.add(ROLE_SYSTEM_OPERATOR)
        }

        return roles
    }

    fun getHasLandlordUserRole(subjectId: String): Boolean {
        val roles = getRolesForSubjectId(subjectId)
        return roles.contains(ROLE_LANDLORD)
    }
}
