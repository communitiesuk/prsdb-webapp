package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_EDITOR
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SystemOperatorRepository

@PrsdbWebService
class UserRolesService(
    val individualLandlordRepository: IndividualLandlordRepository,
    val organisationalLandlordUserRepository: OrganisationalLandlordUserRepository,
    val localCouncilUserRepository: LocalCouncilUserRepository,
    val systemOperatorRepository: SystemOperatorRepository,
) {
    // Note: there is no ROLE_LETTING_AGENT, because the letting agent is not a distinct system user.
    // Letting agents are instead granted access to a particular property during the session
    // when they use their access link to set/enter the property password
    // This is handled by the LettingAgentAccessInterceptor
    fun getLandlordRolesForSubjectId(subjectId: String): List<String> {
        val roles = mutableListOf<String>()

        if (individualLandlordRepository.findByBaseUser_Id(subjectId) != null) {
            roles.add(ROLE_INDIVIDUAL_LANDLORD)
        }

        organisationalLandlordUserRepository.findByBaseUser_Id(subjectId).forEach { orgUser ->
            when (orgUser.role) {
                OrganisationalLandlordUserRole.ADMIN -> roles.add(ROLE_ORG_ADMIN)
                OrganisationalLandlordUserRole.EDITOR -> roles.add(ROLE_ORG_EDITOR)
            }
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

    fun getUserHasLandlordRole(subjectId: String): Boolean = getLandlordRolesForSubjectId(subjectId).isNotEmpty()

    fun getHasLocalCouncilRole(subjectId: String): Boolean {
        val roles = getLocalCouncilRolesForSubjectId(subjectId)
        return roles.contains(ROLE_LOCAL_COUNCIL_USER) || roles.contains(ROLE_LOCAL_COUNCIL_ADMIN)
    }

    fun getHasLocalCouncilAdminRole(subjectId: String): Boolean {
        val roles = getLocalCouncilRolesForSubjectId(subjectId)
        return roles.contains(ROLE_LOCAL_COUNCIL_ADMIN)
    }
}
