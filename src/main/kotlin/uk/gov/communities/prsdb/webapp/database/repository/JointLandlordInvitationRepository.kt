package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import java.time.Instant
import java.util.UUID

interface JointLandlordInvitationRepository : JpaRepository<JointLandlordInvitation, Long> {
    fun findByToken(token: UUID): JointLandlordInvitation?

    fun findByRegisteredOwnership(propertyOwnership: PropertyOwnership): List<JointLandlordInvitation>

    fun findByRegisteredOwnershipId(ownershipId: Long): List<JointLandlordInvitation>

    fun findAllByInvitationExpiredEmailSentFalse(): List<JointLandlordInvitation>

    fun findAllByCreatedDateBefore(cutoffDate: Instant): List<JointLandlordInvitation>

    @Suppress("ktlint:standard:function-naming")
    fun findByRegisteredOwnership_Id(propertyOwnershipId: Long): List<JointLandlordInvitation>
}
