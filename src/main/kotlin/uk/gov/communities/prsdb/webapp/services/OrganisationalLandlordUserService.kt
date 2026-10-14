package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException

@PrsdbWebService
class OrganisationalLandlordUserService(
    private val organisationalLandlordUserRepository: OrganisationalLandlordUserRepository,
    private val userToOrgLandlordUserService: UserToOrgLandlordUserService,
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

    fun getOrganisationalLandlordUsers(organisationalLandlord: OrganisationalLandlord): List<OrganisationalLandlordUser> =
        organisationalLandlordUserRepository.findByOrganisationalLandlord(organisationalLandlord)

    fun getOrganisationalLandlordUserOrNull(id: Long): OrganisationalLandlordUser? =
        organisationalLandlordUserRepository.findById(id).orElse(null)

    fun getCurrentUserIsAuthorizedToEditRecord(otherOrganisationalLandlordUserId: Long): Boolean {
        val currentUser = userToOrgLandlordUserService.getOrganisationalLandlordUserForCurrentUser() ?: return false
        if (currentUser.role != OrganisationalLandlordUserRole.ADMIN) {
            return false
        }

        val member = getOrganisationalLandlordUserOrNull(otherOrganisationalLandlordUserId) ?: return false
        return member.organisationalLandlord.id == currentUser.organisationalLandlord.id
    }

    @Transactional
    fun updateRole(
        id: Long,
        role: OrganisationalLandlordUserRole,
    ) {
        val user =
            getOrganisationalLandlordUserOrNull(id)
                ?: throw PrsdbWebException("No organisational landlord user found with id $id")
        user.role = role
        organisationalLandlordUserRepository.save(user)
    }
}
