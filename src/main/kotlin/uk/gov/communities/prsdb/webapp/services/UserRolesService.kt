package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SystemOperatorRepository

@PrsdbWebService
class UserRolesService(
    val landlordRepository: LandlordRepository,
    val localAuthorityUserRepository: LocalAuthorityUserRepository,
    val systemOperatorRepository: SystemOperatorRepository,
) {
    fun getLandlordRolesForSubjectId(subjectId: String): List<String> {
        val roles = mutableListOf<String>()

        val matchingLandlordUser = landlordRepository.findByBaseUser_Id(subjectId)
        if (matchingLandlordUser != null) {
            roles.add(ROLE_LANDLORD)
        }

        return roles
    }

    fun getLocalAuthorityRolesForSubjectId(subjectId: String): List<String> {
        val roles = mutableListOf<String>()

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

    fun getAllRolesForSubjectId(subjectId: String): List<String> =
        getLandlordRolesForSubjectId(subjectId) +
            getLocalAuthorityRolesForSubjectId(subjectId)

    fun getHasLandlordUserRole(subjectId: String): Boolean {
        val roles = getAllRolesForSubjectId(subjectId)
        return roles.contains(ROLE_LANDLORD)
    }

    fun getHasLocalAuthorityRole(subjectId: String): Boolean {
        val roles = getAllRolesForSubjectId(subjectId)
        return roles.contains(ROLE_LA_USER) || roles.contains(ROLE_LA_ADMIN)
    }

    fun getHasLocalAuthorityAdminRole(subjectId: String): Boolean {
        val roles = getAllRolesForSubjectId(subjectId)
        return roles.contains(ROLE_LA_ADMIN)
    }
}
