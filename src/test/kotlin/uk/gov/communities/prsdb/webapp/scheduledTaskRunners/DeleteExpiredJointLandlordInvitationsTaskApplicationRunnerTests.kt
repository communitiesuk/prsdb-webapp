package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.application.DeleteExpiredJointLandlordInvitationsTaskApplicationRunner
import uk.gov.communities.prsdb.webapp.application.DeleteExpiredJointLandlordInvitationsTaskApplicationRunner.Companion.DELETE_EXPIRED_JL_INVITATIONS_TASK_METHOD_NAME
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationDeletionService

@ExtendWith(MockitoExtension::class)
class DeleteExpiredJointLandlordInvitationsTaskApplicationRunnerTests {
    @Mock
    private lateinit var context: ApplicationContext

    @Mock
    private lateinit var jointLandlordInvitationDeletionService: JointLandlordInvitationDeletionService

    @InjectMocks
    private lateinit var runner: DeleteExpiredJointLandlordInvitationsTaskApplicationRunner

    @Test
    fun `deleteExpiredJointLandlordInvitationsTaskLogic calls service to delete expired invitations`() {
        // Arrange
        val method =
            DeleteExpiredJointLandlordInvitationsTaskApplicationRunner::class.java
                .getDeclaredMethod(DELETE_EXPIRED_JL_INVITATIONS_TASK_METHOD_NAME)
        method.isAccessible = true

        // Act
        method.invoke(runner)

        // Assert
        verify(jointLandlordInvitationDeletionService).deleteExpiredInvitations()
    }
}
