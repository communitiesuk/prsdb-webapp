package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_DELETION_GRACE_PERIOD_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

@PrsdbTaskService
class OrganisationalLandlordInvitationDeletionService(
    private val invitationRepository: OrganisationalLandlordInvitationRepository,
) {
    fun deleteExpiredInvitations(): List<Long> {
        val retentionPeriodInDays =
            (
                ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS +
                    ORGANISATIONAL_LANDLORD_INVITATION_DELETION_GRACE_PERIOD_IN_DAYS
            ).toLong()
        val cutoffDate = Instant.now().minus(retentionPeriodInDays, ChronoUnit.DAYS)

        val invitationsToDelete =
            invitationRepository
                .findAllByCreatedDateBefore(cutoffDate)
                .filter { it.status in listOf(OrganisationalLandlordInvitationStatus.EXPIRED, OrganisationalLandlordInvitationStatus.HIDDEN) }

        val deletedIds = invitationsToDelete.map { it.id }
        invitationRepository.deleteAll(invitationsToDelete)

        return deletedIds
    }
}
