package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.application.JointLandlordInvitationExpiryEmailTaskLogic
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationExpiryEmailResult
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationExpiryEmailService

@ExtendWith(MockitoExtension::class)
class JointLandlordInvitationExpiryEmailTaskApplicationRunnerTests {
    @Mock
    private lateinit var jointLandlordInvitationExpiryEmailService: JointLandlordInvitationExpiryEmailService

    @InjectMocks
    private lateinit var taskLogic: JointLandlordInvitationExpiryEmailTaskLogic

    @Test
    fun `sendJointLandlordInvitationExpiryEmails calls service to send expiry emails`() {
        // Arrange
        whenever(jointLandlordInvitationExpiryEmailService.sendExpiryEmailsForExpiredInvitations())
            .thenReturn(JointLandlordInvitationExpiryEmailResult(sentIds = emptyList(), failedIds = emptyList()))

        // Act
        taskLogic.sendJointLandlordInvitationExpiryEmails()

        // Assert
        verify(jointLandlordInvitationExpiryEmailService).sendExpiryEmailsForExpiredInvitations()
    }

    @Test
    fun `sendJointLandlordInvitationExpiryEmails returns zero when no emails fail`() {
        // Arrange
        whenever(jointLandlordInvitationExpiryEmailService.sendExpiryEmailsForExpiredInvitations())
            .thenReturn(JointLandlordInvitationExpiryEmailResult(sentIds = listOf(1L, 2L), failedIds = emptyList()))

        // Act
        val failureCount = taskLogic.sendJointLandlordInvitationExpiryEmails()

        // Assert
        assertEquals(0, failureCount)
    }

    @Test
    fun `sendJointLandlordInvitationExpiryEmails returns the number of failed emails`() {
        // Arrange
        whenever(jointLandlordInvitationExpiryEmailService.sendExpiryEmailsForExpiredInvitations())
            .thenReturn(JointLandlordInvitationExpiryEmailResult(sentIds = listOf(1L), failedIds = listOf(2L, 3L)))

        // Act
        val failureCount = taskLogic.sendJointLandlordInvitationExpiryEmails()

        // Assert
        assertEquals(2, failureCount)
    }
}
