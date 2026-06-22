package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.application.JointLandlordInvitationExpiryEmailTaskLogic
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationExpiryEmailService

@ExtendWith(MockitoExtension::class)
class JointLandlordInvitationExpiryEmailTaskApplicationRunnerTests {
    @Mock
    private lateinit var jointLandlordInvitationExpiryEmailService: JointLandlordInvitationExpiryEmailService

    @InjectMocks
    private lateinit var taskLogic: JointLandlordInvitationExpiryEmailTaskLogic

    @Test
    fun `sendJointLandlordInvitationExpiryEmails calls service to send expiry emails`() {
        // Act
        taskLogic.sendJointLandlordInvitationExpiryEmails()

        // Assert
        verify(jointLandlordInvitationExpiryEmailService).sendExpiryEmailsForExpiredInvitations()
    }
}
