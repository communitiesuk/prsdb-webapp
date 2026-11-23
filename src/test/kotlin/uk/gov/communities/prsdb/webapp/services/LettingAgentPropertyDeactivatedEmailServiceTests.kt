package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LettingAgentPropertyDeactivatedNotificationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class LettingAgentPropertyDeactivatedEmailServiceTests {
    @Mock
    private lateinit var mockEmailNotificationService: EmailNotificationService<LettingAgentPropertyDeactivatedNotificationEmail>

    @InjectMocks
    private lateinit var lettingAgentPropertyDeactivatedEmailService: LettingAgentPropertyDeactivatedEmailService

    @Test
    fun `sendLettingAgentPropertyDeactivatedEmail sends the property deactivated email to the letting agent`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 5)

        lettingAgentPropertyDeactivatedEmailService.sendLettingAgentPropertyDeactivatedEmail(propertyOwnership, AGENT_EMAIL)

        verify(mockEmailNotificationService).sendEmail(
            AGENT_EMAIL,
            LettingAgentPropertyDeactivatedNotificationEmail(
                propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                singleLineAddress = propertyOwnership.address.singleLineAddress,
            ),
        )
    }

    companion object {
        private const val AGENT_EMAIL = "agent@example.com"
    }
}
