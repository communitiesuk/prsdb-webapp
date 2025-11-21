package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SystemOperatorRepository

@PrsdbWebService
class UserRolesService(
    val landlordRepository: LandlordRepository,
    val localCouncilUserRepository: LocalCouncilUserRepository,
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

    fun getLocalCouncilRolesForSubjectId(subjectId: String): List<String> {
        val roles = mutableListOf<String>()

        val matchingLocalCouncilUser = localCouncilUserRepository.findByBaseUser_Id(subjectId)
        if (matchingLocalCouncilUser != null) {
            if (matchingLocalCouncilUser.isManager) {
                roles.add(ROLE_LOCAL_COUNCIL_ADMIN)
            }
            roles.add(ROLE_LOCAL_COUNCIL_USER)
        }

        val matchingSystemOperator = systemOperatorRepository.findByBaseUser_Id(subjectId)
        if (matchingSystemOperator != null) {
            roles.add(ROLE_SYSTEM_OPERATOR)
        }

        return roles
    }

    fun getAllRolesForSubjectId(subjectId: String): List<String> =
        getLandlordRolesForSubjectId(subjectId) +
            getLocalCouncilRolesForSubjectId(subjectId)

    fun getHasLandlordUserRole(subjectId: String): Boolean {
        val roles = getLandlordRolesForSubjectId(subjectId)
        return roles.contains(ROLE_LANDLORD)
    }

    fun getHasLocalCouncilRole(subjectId: String): Boolean {
        val roles = getLocalCouncilRolesForSubjectId(subjectId)
        return roles.contains(ROLE_LOCAL_COUNCIL_USER) || roles.contains(ROLE_LOCAL_COUNCIL_ADMIN)
    }

    fun getHasLocalCouncilAdminRole(subjectId: String): Boolean {
        val roles = getLocalCouncilRolesForSubjectId(subjectId)
        return roles.contains(ROLE_LOCAL_COUNCIL_ADMIN)
    }
}
