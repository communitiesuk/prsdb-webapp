package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.application.JointLandlordInvitationExpiryEmailTaskApplicationRunner
import uk.gov.communities.prsdb.webapp.application.JointLandlordInvitationExpiryEmailTaskApplicationRunner.Companion.SEND_JOINT_LANDLORD_INVITATION_EXPIRY_EMAILS_TASK_METHOD_NAME
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationExpiryEmailService

@ExtendWith(MockitoExtension::class)
class JointLandlordInvitationExpiryEmailTaskApplicationRunnerTests {
    @Mock
    private lateinit var context: ApplicationContext

    @Mock
    private lateinit var jointLandlordInvitationExpiryEmailService: JointLandlordInvitationExpiryEmailService

    @InjectMocks
    private lateinit var runner: JointLandlordInvitationExpiryEmailTaskApplicationRunner

    @Test
    fun `sendJointLandlordInvitationExpiryEmailsTaskLogic calls service to send expiry emails`() {
        // Arrange
        val method =
            JointLandlordInvitationExpiryEmailTaskApplicationRunner::class.java
                .getDeclaredMethod(SEND_JOINT_LANDLORD_INVITATION_EXPIRY_EMAILS_TASK_METHOD_NAME)
        method.isAccessible = true

        // Act
        method.invoke(runner)

        // Assert
        verify(jointLandlordInvitationExpiryEmailService).sendExpiryEmailsForExpiredInvitations()
    }
}
