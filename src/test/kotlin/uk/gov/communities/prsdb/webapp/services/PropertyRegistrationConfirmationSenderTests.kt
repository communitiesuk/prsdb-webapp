package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_PHASE_TWO
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyRegistrationData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class PropertyRegistrationConfirmationSenderTests {
    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockConfirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>

    @Mock
    private lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    private lateinit var mockCurrentUserService: CurrentUserService

    @InjectMocks
    private lateinit var confirmationSender: PropertyRegistrationConfirmationSender

    private val currentUserEmail = "admin@example.com"
    private val dashboardUri = URI("https://gov.uk")

    @Test
    fun `sendConfirmationEmail sends the email to the current user with the property and dashboard details`() {
        // Arrange
        val landlord = MockLandlordData.createIndividualLandlord()
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 5678)
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(landlord),
                registrationNumber = registrationNumber,
                currentNumTenants = 2,
            )
        whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(currentUserEmail)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)

        // Act
        confirmationSender.sendConfirmationEmail(
            MockPropertyRegistrationData.createPropertyRegistrationDataModel(),
            landlord,
            propertyOwnership,
        )

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            eq(currentUserEmail),
            argThat<PropertyRegistrationConfirmationEmail> { email ->
                email.prn == RegistrationNumberDataModel.fromRegistrationNumber(registrationNumber).toString() &&
                    email.multiLineAddress == propertyOwnership.address.toMultiLineAddress() &&
                    email.prsdUrl == dashboardUri.toString() &&
                    email.isOccupied == propertyOwnership.isOccupied
            },
        )
    }

    @Test
    fun `sendConfirmationEmail includes the joint landlord emails and delegation status from the registration data`() {
        // Arrange
        val landlord = MockLandlordData.createIndividualLandlord()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord))
        val jointLandlordEmails = listOf("joint1@example.com", "joint2@example.com")
        whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(currentUserEmail)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)

        // Act
        confirmationSender.sendConfirmationEmail(
            MockPropertyRegistrationData.createPropertyRegistrationDataModel(
                jointLandlordEmails = jointLandlordEmails,
                isDelegatedToLettingAgent = true,
            ),
            landlord,
            propertyOwnership,
        )

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            eq(currentUserEmail),
            argThat<PropertyRegistrationConfirmationEmail> { email ->
                email.jointLandlordEmails == jointLandlordEmails && email.isDelegatedToLettingAgent
            },
        )
    }

    @Test
    fun `sendConfirmationEmail passes the provide-later flags and enabled phase two flag to the email`() {
        // Arrange
        val landlord = MockLandlordData.createIndividualLandlord()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord))
        whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(currentUserEmail)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
        whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_PHASE_TWO)).thenReturn(true)

        // Act
        confirmationSender.sendConfirmationEmail(
            MockPropertyRegistrationData.createPropertyRegistrationDataModel(
                licenseProvideLater = true,
                gasSafetyCertProvideLater = true,
                electricalSafetyCertProvideLater = true,
                epcProvideLater = true,
                tenancyProvideLater = true,
            ),
            landlord,
            propertyOwnership,
        )

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            eq(currentUserEmail),
            argThat<PropertyRegistrationConfirmationEmail> { email ->
                email.isPdjb939PhaseTwoEnabled &&
                    email.licenseProvideLater &&
                    email.gasSafetyCertProvideLater &&
                    email.electricalSafetyCertProvideLater &&
                    email.epcProvideLater &&
                    email.tenancyProvideLater
            },
        )
    }

    @Test
    fun `sendConfirmationEmail marks phase two as disabled when the feature flag is off`() {
        // Arrange
        val landlord = MockLandlordData.createIndividualLandlord()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord))
        whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(currentUserEmail)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
        whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_PHASE_TWO)).thenReturn(false)

        // Act
        confirmationSender.sendConfirmationEmail(
            MockPropertyRegistrationData.createPropertyRegistrationDataModel(),
            landlord,
            propertyOwnership,
        )

        // Assert
        verify(mockConfirmationEmailSender).sendEmail(
            eq(currentUserEmail),
            argThat<PropertyRegistrationConfirmationEmail> { email -> !email.isPdjb939PhaseTwoEnabled },
        )
    }
}
