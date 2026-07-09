package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember

@Repository
@Suppress("ktlint:standard:function-naming")
interface OrganisationGoverningBodyMemberRepository : JpaRepository<OrganisationGoverningBodyMember, Long> {
    fun findAllByOrganisationLandlord_Id(organisationLandlordId: Long): List<OrganisationGoverningBodyMember>
}
