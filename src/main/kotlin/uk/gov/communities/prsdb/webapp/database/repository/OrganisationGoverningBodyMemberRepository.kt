package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord

@Repository
@Suppress("ktlint:standard:function-naming")
interface OrganisationGoverningBodyMemberRepository : JpaRepository<OrganisationGoverningBodyMember, Long> {
    fun deleteByOrganisationalLandlord_Id(organisationalLandlordId: Long)

    fun deleteByOrganisationalLandlord(organisationalLandlord: OrganisationalLandlord)
}
