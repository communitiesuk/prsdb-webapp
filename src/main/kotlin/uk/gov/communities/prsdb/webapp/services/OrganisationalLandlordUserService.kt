package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository

@PrsdbWebService
class OrganisationalLandlordUserService(
    private val organisationalLandlordUserRepository: OrganisationalLandlordUserRepository,
) {
    @Transactional
    fun createOrganisationalLandlordUser(
        organisationalLandlord: OrganisationalLandlord,
        baseUser: PrsdbUser,
        name: String,
        email: String,
        role: OrganisationalLandlordUserRole,
    ): OrganisationalLandlordUser =
        organisationalLandlordUserRepository.save(
            OrganisationalLandlordUser(organisationalLandlord, baseUser, name, email, role),
        )

    fun getCurrentUsersRoleForOrgOrNull(organisationalLandlord: OrganisationalLandlord): OrganisationalLandlordUserRole? {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        return organisationalLandlordUserRepository
            .findByBaseUser_IdAndOrganisationalLandlord_Id(baseUserId, organisationalLandlord.id)
            ?.role
    }

    fun throwIfCurrentUserIsNotAdminOfOrg(organisationalLandlord: OrganisationalLandlord) {
        if (getCurrentUsersRoleForOrgOrNull(organisationalLandlord) != OrganisationalLandlordUserRole.ADMIN) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "The current user is not an admin of this organisation",
            )
        }
    }
}
