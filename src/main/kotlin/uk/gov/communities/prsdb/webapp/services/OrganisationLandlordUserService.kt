package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationLandlordUserRepository

@PrsdbWebService
class OrganisationLandlordUserService(
    private val organisationLandlordUserRepository: OrganisationLandlordUserRepository,
) {
    @Transactional
    fun createOrganisationLandlordUser(
        organisationLandlord: OrganisationLandlord,
        baseUser: PrsdbUser,
        name: String,
        email: String,
    ): OrganisationLandlordUser =
        organisationLandlordUserRepository.save(
            OrganisationLandlordUser(organisationLandlord, baseUser, name, email),
        )
}
