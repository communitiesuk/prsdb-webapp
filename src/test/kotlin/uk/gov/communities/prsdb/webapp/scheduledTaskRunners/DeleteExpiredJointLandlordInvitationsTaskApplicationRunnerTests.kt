package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.application.DeleteExpiredJointLandlordInvitationsTaskLogic
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationDeletionService

@ExtendWith(MockitoExtension::class)
class DeleteExpiredJointLandlordInvitationsTaskApplicationRunnerTests {
    @Mock
    private lateinit var jointLandlordInvitationDeletionService: JointLandlordInvitationDeletionService

    @InjectMocks
    private lateinit var taskLogic: DeleteExpiredJointLandlordInvitationsTaskLogic

    @Test
    fun `deleteJointLandlordInvitationsPastRetentionPeriod calls deletion service`() {
        // Act
        taskLogic.deleteJointLandlordInvitationsPastRetentionPeriod()

        // Assert
        verify(jointLandlordInvitationDeletionService).deleteInvitationsPastRetentionPeriod()
    }
}
