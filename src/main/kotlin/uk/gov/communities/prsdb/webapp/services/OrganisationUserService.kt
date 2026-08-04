package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationUserRepository

@PrsdbWebService
class OrganisationUserService(
    private val organisationUserRepository: OrganisationUserRepository,
) {
    @Transactional
    fun createOrganisationUser(
        organisationLandlord: OrganisationLandlord,
        baseUser: PrsdbUser,
        name: String,
        email: String,
    ): OrganisationUser =
        organisationUserRepository.save(
            OrganisationUser(organisationLandlord, baseUser, name, email),
        )
}
