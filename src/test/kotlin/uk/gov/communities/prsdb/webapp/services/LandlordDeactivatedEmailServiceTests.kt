package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordDeactivatedEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class LandlordDeactivatedEmailServiceTests {
    @Mock
    private lateinit var landlordDeactivatedEmailSender: EmailNotificationService<LandlordDeactivatedEmail>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @InjectMocks
    private lateinit var landlordDeactivatedEmailService: LandlordDeactivatedEmailService

    @Test
    fun `sendLandlordDeactivatedEmail emails the landlord with their details, the reason and a dashboard link`() {
        // Arrange
        val landlord =
            MockLandlordData.createIndividualLandlord(
                name = "Jill Jones",
                registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 123456L),
            )
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(DASHBOARD_URI)

        // Act
        landlordDeactivatedEmailService.sendLandlordDeactivatedEmail(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED)

        // Assert
        verify(landlordDeactivatedEmailSender).sendEmailToLandlord(
            landlord,
            LandlordDeactivatedEmail(
                recipientName = "Jill Jones",
                registrationNumber = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 123456L).toString(),
                reason = LandlordDeactivationReason.NO_PROPERTIES_REGISTERED,
                dashboardUrl = DASHBOARD_URI.toString(),
            ),
        )
    }

    @Test
    fun `sendLandlordDeactivatedEmail does not throw when the email fails to send`() {
        // Arrange
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(DASHBOARD_URI)
        whenever(landlordDeactivatedEmailSender.sendEmailToLandlord(any(), any())).doThrow(RuntimeException("Notify down"))

        // Act, Assert
        assertDoesNotThrow {
            landlordDeactivatedEmailService.sendLandlordDeactivatedEmail(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED)
        }
    }

    companion object {
        private val DASHBOARD_URI = URI("https://example.com/landlord/dashboard")
    }
}
