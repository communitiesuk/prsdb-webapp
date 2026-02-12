package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import java.util.UUID

interface JointLandlordInvitationRepository : JpaRepository<JointLandlordInvitation, Long> {
    fun findByToken(token: UUID): JointLandlordInvitation?
}
