package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.AutomatedPropertyDeactivationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordDeactivatedEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LettingAgentPropertyDeactivatedNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeactivationInviteeCancellationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class DeactivationEmailServiceTests {
    @Mock
    private lateinit var mockLettingAgentEmailSender: EmailNotificationService<LettingAgentPropertyDeactivatedNotificationEmail>

    @Mock
    private lateinit var mockLandlordEmailSender: EmailNotificationService<LandlordDeactivatedEmail>

    @Mock
    private lateinit var mockPropertyEmailSender: EmailNotificationService<AutomatedPropertyDeactivationEmail>

    @Mock
    private lateinit var mockInviteeCancellationEmailSender: EmailNotificationService<PropertyDeactivationInviteeCancellationEmail>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    private val deactivationEmailService: DeactivationEmailService
        get() =
            DeactivationEmailService(
                mockLettingAgentEmailSender,
                mockLandlordEmailSender,
                mockPropertyEmailSender,
                mockInviteeCancellationEmailSender,
                absoluteUrlProvider,
            )

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
    fun `sendLettingAgentPropertyDeactivatedEmail does not throw when sending the email fails`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 5)
        doThrow(IllegalStateException("Email delivery failed"))
            .whenever(mockLettingAgentEmailSender)
            .sendEmail(any(), any())

        deactivationEmailService.sendLettingAgentPropertyDeactivatedEmail(propertyOwnership, AGENT_EMAIL)

        verify(mockLettingAgentEmailSender).sendEmail(any(), any())
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

    @Test
    fun `sendLandlordDeactivatedEmail does not throw when sending the email fails`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https://example.com/landlord/dashboard"))
        doThrow(IllegalStateException("Email delivery failed"))
            .whenever(mockLandlordEmailSender)
            .sendEmailToLandlord(any(), any())

        deactivationEmailService.sendLandlordDeactivatedEmail(landlord, LandlordDeactivationReason.NO_PROPERTIES_REGISTERED)

        verify(mockLandlordEmailSender).sendEmailToLandlord(any(), any())
    }

    @Test
    fun `sendPropertyDeactivatedEmail sends each landlord an email with their account status`() {
        val individual = MockLandlordData.createIndividualLandlord(name = "Individual", email = "individual@example.com")
        val organisation = MockLandlordData.createOrgLandlord(name = "Organisation")
        val property = MockLandlordData.createPropertyOwnership(id = 42L, landlords = mutableSetOf(individual, organisation))

        val reasons = mutableListOf(ReactivationTaskType.GAS_SAFETY, ReactivationTaskType.TENANCY_DETAILS)

        val reactivationUrl = "https://example.test/landlord/reactivate/42"
        whenever(absoluteUrlProvider.buildPropertyReactivationUri(42L)).thenReturn(URI(reactivationUrl))

        deactivationEmailService.sendPropertyDeactivatedEmail(property, listOf(individual to true, organisation to false), reasons)

        verify(mockPropertyEmailSender).sendEmailToLandlord(
            individual,
            AutomatedPropertyDeactivationEmail(
                landlordName = individual.name,
                multiLineAddress = property.address.toMultiLineAddress(),
                prn = RegistrationNumberDataModel.fromRegistrationNumber(property.registrationNumber).toString(),
                reactivationUrl = reactivationUrl,
                reasons = reasons,
                landlordDeactivated = true,
            ),
        )
        verify(mockPropertyEmailSender).sendEmailToLandlord(
            organisation,
            AutomatedPropertyDeactivationEmail(
                landlordName = organisation.name,
                multiLineAddress = property.address.toMultiLineAddress(),
                prn = RegistrationNumberDataModel.fromRegistrationNumber(property.registrationNumber).toString(),
                reactivationUrl = reactivationUrl,
                reasons = reasons,
                landlordDeactivated = false,
            ),
        )
    }

    @Test
    fun `sendPropertyDeactivatedEmail continues notifying other landlords when one notification fails`() {
        val firstLandlord = MockLandlordData.createIndividualLandlord(name = "First")
        val secondLandlord = MockLandlordData.createIndividualLandlord(name = "Second")
        val property = MockLandlordData.createPropertyOwnership(id = 42L, landlords = mutableSetOf(firstLandlord, secondLandlord))

        whenever(absoluteUrlProvider.buildPropertyReactivationUri(42L)).thenReturn(URI("https://example.test/landlord/reactivate/42"))

        doThrow(IllegalStateException("delivery failed"))
            .whenever(mockPropertyEmailSender)
            .sendEmailToLandlord(eq(firstLandlord), any())

        deactivationEmailService
            .sendPropertyDeactivatedEmail(
                property,
                listOf(firstLandlord to true, secondLandlord to false),
                listOf(ReactivationTaskType.TENANCY_DETAILS),
            )

        verify(mockPropertyEmailSender).sendEmailToLandlord(eq(secondLandlord), any())
    }

    @Test
    fun `sendInviteeCancellationEmail sends the invitation cancelled email to the invitee`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 5)

        deactivationEmailService.sendInviteeCancellationEmail(propertyOwnership, INVITEE_EMAIL)

        verify(mockInviteeCancellationEmailSender).sendEmail(
            INVITEE_EMAIL,
            PropertyDeactivationInviteeCancellationEmail(multiLineAddress = propertyOwnership.address.toMultiLineAddress()),
        )
    }

    @Test
    fun `sendInviteeCancellationEmail does not throw when sending the email fails`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 5)
        doThrow(IllegalStateException("Email delivery failed"))
            .whenever(mockInviteeCancellationEmailSender)
            .sendEmail(any(), any())

        deactivationEmailService.sendInviteeCancellationEmail(propertyOwnership, INVITEE_EMAIL)

        verify(mockInviteeCancellationEmailSender).sendEmail(any(), any())
    }

    companion object {
        private const val AGENT_EMAIL = "agent@example.com"
        private const val INVITEE_EMAIL = "invitee@example.com"
    }
}
