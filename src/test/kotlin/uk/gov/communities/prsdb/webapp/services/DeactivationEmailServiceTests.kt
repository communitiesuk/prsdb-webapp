package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordDeactivatedEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LettingAgentPropertyDeactivatedNotificationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class DeactivationEmailServiceTests {
    @Mock
    private lateinit var mockLettingAgentEmailSender: EmailNotificationService<LettingAgentPropertyDeactivatedNotificationEmail>

    @Mock
    private lateinit var mockLandlordEmailSender: EmailNotificationService<LandlordDeactivatedEmail>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    private val deactivationEmailService: DeactivationEmailService
        get() = DeactivationEmailService(mockLettingAgentEmailSender, mockLandlordEmailSender, absoluteUrlProvider)

    @Test
    fun `sendLettingAgentPropertyDeactivatedEmail sends the property deactivated email to the letting agent`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 5)

        deactivationEmailService.sendLettingAgentPropertyDeactivatedEmail(propertyOwnership, AGENT_EMAIL)

        verify(mockLettingAgentEmailSender).sendEmail(
            AGENT_EMAIL,
            LettingAgentPropertyDeactivatedNotificationEmail(
                propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                singleLineAddress = propertyOwnership.address.singleLineAddress,
            ),
        )
    }

    @Test
    fun `sendLandlordDeactivatedEmail sends the landlord account deactivation email`() {
        val landlord =
            MockLandlordData.createIndividualLandlord(
                name = "Jill Jones",
                registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 123456L),
            )
        val dashboardUri = URI("https://example.com/landlord/dashboard")
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)

        deactivationEmailService.sendLandlordDeactivatedEmail(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED)

        verify(mockLandlordEmailSender).sendEmailToLandlord(
            landlord,
            LandlordDeactivatedEmail(
                recipientName = "Jill Jones",
                registrationNumber = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 123456L).toString(),
                reason = LandlordDeactivationReason.NO_PROPERTIES_REGISTERED,
                dashboardUrl = dashboardUri.toString(),
            ),
        )
    }

    companion object {
        private const val AGENT_EMAIL = "agent@example.com"
    }
}
