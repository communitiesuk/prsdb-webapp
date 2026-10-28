package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import java.time.Instant
import java.util.UUID

interface OrganisationalLandlordInvitationRepository : JpaRepository<OrganisationalLandlordInvitation, Long> {
    fun findByToken(token: UUID): OrganisationalLandlordInvitation?

    fun findByOrganisationalLandlord(organisationalLandlord: OrganisationalLandlord): List<OrganisationalLandlordInvitation>

    fun findAllByCreatedDateBefore(cutoffDate: Instant): List<OrganisationalLandlordInvitation>

    fun deleteByOrganisationalLandlord(organisationalLandlord: OrganisationalLandlord)
}
