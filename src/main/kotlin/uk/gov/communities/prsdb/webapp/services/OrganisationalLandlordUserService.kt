package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
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
}
