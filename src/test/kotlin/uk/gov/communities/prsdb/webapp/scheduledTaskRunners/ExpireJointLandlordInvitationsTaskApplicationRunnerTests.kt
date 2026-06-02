package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.application.ExpireJointLandlordInvitationsTaskApplicationRunner
import uk.gov.communities.prsdb.webapp.application.ExpireJointLandlordInvitationsTaskApplicationRunner.Companion.EXPIRE_JOINT_LANDLORD_INVITATIONS_TASK_METHOD_NAME
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationExpiryService

@ExtendWith(MockitoExtension::class)
class ExpireJointLandlordInvitationsTaskApplicationRunnerTests {
    @Mock
    private lateinit var context: ApplicationContext

    @Mock
    private lateinit var jointLandlordInvitationExpiryService: JointLandlordInvitationExpiryService

    @InjectMocks
    private lateinit var runner: ExpireJointLandlordInvitationsTaskApplicationRunner

    @Test
    fun `expireJointLandlordInvitationsTaskLogic calls service to expire pending invitations`() {
        // Arrange
        val method =
            ExpireJointLandlordInvitationsTaskApplicationRunner::class.java
                .getDeclaredMethod(EXPIRE_JOINT_LANDLORD_INVITATIONS_TASK_METHOD_NAME)
        method.isAccessible = true

        // Act
        method.invoke(runner)

        // Assert
        verify(jointLandlordInvitationExpiryService).expirePendingInvitations()
    }
}
