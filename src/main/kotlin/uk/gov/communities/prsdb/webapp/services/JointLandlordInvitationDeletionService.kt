package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_DELETION_GRACE_PERIOD_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.JointLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

@PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "jl-invitation-deletion-flag-on")
interface JointLandlordInvitationDeletionService {
    fun deleteExpiredInvitations(): List<Long>
}

@Primary
@PrsdbTaskService("jl-invitation-deletion-flag-off")
class JointLandlordInvitationDeletionServiceImplFlagOff : JointLandlordInvitationDeletionService {
    override fun deleteExpiredInvitations(): List<Long> = emptyList()
}

@PrsdbTaskService("jl-invitation-deletion-flag-on")
class JointLandlordInvitationDeletionServiceImplFlagOn(
    private val invitationRepository: JointLandlordInvitationRepository,
) : JointLandlordInvitationDeletionService {
    @Transactional
    override fun deleteExpiredInvitations(): List<Long> {
        val totalGracePeriodInDays =
            (JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + JOINT_LANDLORD_INVITATION_DELETION_GRACE_PERIOD_IN_DAYS).toLong()
        val cutoffDate = Instant.now().minus(totalGracePeriodInDays, ChronoUnit.DAYS)

        val invitationsToDelete =
            invitationRepository
                .findAllByCreatedDateBefore(cutoffDate)
                .filter { it.status != JointLandlordInvitationStatus.PENDING }

        val deletedIds = invitationsToDelete.map { it.id }
        invitationRepository.deleteAll(invitationsToDelete)

        return deletedIds
    }
}
