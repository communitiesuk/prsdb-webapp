package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository

@PrsdbWebService
class OrganisationalLandlordUserService(
    private val organisationalLandlordUserRepository: OrganisationalLandlordUserRepository,
) {
    @Transactional
    fun createOrganisationalLandlordUser(
        organisationLandlord: OrganisationLandlord,
        baseUser: PrsdbUser,
        name: String,
        email: String,
    ): OrganisationalLandlordUser =
        organisationalLandlordUserRepository.save(
            OrganisationalLandlordUser(organisationLandlord, baseUser, name, email),
        )
}
