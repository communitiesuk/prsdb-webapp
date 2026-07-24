package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationLandlordUserRepository

@PrsdbWebService
class OrganisationLandlordUserService(
    private val organisationLandlordUserRepository: OrganisationLandlordUserRepository,
) {
    fun createOrganisationLandlordUser(
        organisationLandlord: OrganisationLandlord,
        baseUser: PrsdbUser,
    ): OrganisationLandlordUser = organisationLandlordUserRepository.save(OrganisationLandlordUser(organisationLandlord, baseUser))
}
