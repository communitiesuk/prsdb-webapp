package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_DELETION_GRACE_PERIOD_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.JointLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

@PrsdbTaskService
class JointLandlordInvitationDeletionService(
    private val invitationRepository: JointLandlordInvitationRepository,
    private val featureFlagManager: FeatureFlagManager,
) {
    @Transactional
    fun deleteExpiredInvitations(): List<Long> {
        if (!featureFlagManager.checkFeature(JOINT_LANDLORDS)) {
            return emptyList()
        }

        val totalGracePeriodInDays =
            (JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + JOINT_LANDLORD_INVITATION_DELETION_GRACE_PERIOD_IN_DAYS).toLong()
        val cutoffDate = Instant.now().minus(totalGracePeriodInDays, ChronoUnit.DAYS)

        val invitationsToDelete =
            invitationRepository
                .findAllByCreatedDateBefore(cutoffDate)
                .filter { it.status in listOf(JointLandlordInvitationStatus.EXPIRED, JointLandlordInvitationStatus.HIDDEN) }

        val deletedIds = invitationsToDelete.map { it.id }
        invitationRepository.deleteAll(invitationsToDelete)

        return deletedIds
    }
}
