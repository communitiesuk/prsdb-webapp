package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationConfirmationEmailRedesign
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationInviteeCancellationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class ConfirmStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockPropertyDeregistrationService: PropertyDeregistrationService

    @Mock
    lateinit var mockJointLandlordInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    lateinit var mockConfirmationEmailSender: EmailNotificationService<PropertyDeregistrationConfirmationEmailRedesign>

    @Mock
    lateinit var mockInviteeCancellationEmailSender: EmailNotificationService<PropertyDeregistrationInviteeCancellationEmail>

    @Mock
    lateinit var mockState: PropertyDeregistrationJourneyState

    val propertyOwnershipId = 123L

    @Test
    fun `afterStepDataIsAdded deregisters the property`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
            .thenReturn(MockLandlordData.createPropertyOwnership())

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockPropertyDeregistrationService).deregisterProperty(propertyOwnershipId)
    }

    @Test
    fun `afterStepDataIsAdded adds deregistered property ownership id and address to session`() {
        val stepConfig = setupStepConfig()
        val propertyAddress = "123 Test Street, AB1 2CD"
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
            .thenReturn(
                MockLandlordData.createPropertyOwnership(address = MockLandlordData.createAddress(singleLineAddress = propertyAddress)),
            )

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockPropertyDeregistrationService).addDeregisteredPropertyOwnershipIdToSession(propertyOwnershipId, propertyAddress)
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email to the landlord`() {
        val stepConfig = setupStepConfig()
        val james = MockLandlordData.createLandlord(name = "James", email = "james@example.com")
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
            .thenReturn(MockLandlordData.createPropertyOwnership(primaryLandlord = james))

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockConfirmationEmailSender).sendEmail(eq("james@example.com"), any<PropertyDeregistrationConfirmationEmailRedesign>())
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email with correct landlord name and address`() {
        val stepConfig = setupStepConfig()
        val multiLineAddress = "123 Test Street\nAB1 2CD"
        val james = MockLandlordData.createLandlord(name = "James", email = "james@example.com")
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
            .thenReturn(
                MockLandlordData.createPropertyOwnership(
                    primaryLandlord = james,
                    address = MockLandlordData.createAddress(singleLineAddress = "123 Test Street, AB1 2CD"),
                ),
            )

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockConfirmationEmailSender).sendEmail(
            eq("james@example.com"),
            argThat<PropertyDeregistrationConfirmationEmailRedesign> {
                this.landlordName == "James" && this.multiLineAddress == multiLineAddress
            },
        )
    }

    @Test
    fun `afterStepDataIsAdded sends invitee cancellation email to each cancelled invitee`() {
        val stepConfig = setupStepConfig()
        val multiLineAddress = "123 Test Street\nAB1 2CD"
        val signInUrl = "https://example.com/dashboard"
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                address = MockLandlordData.createAddress(singleLineAddress = "123 Test Street, AB1 2CD"),
            )
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockJointLandlordInvitationService.getPendingInvitations(propertyOwnership))
            .thenReturn(
                listOf(
                    MockJointLandlordData.createJointLandlordInvitation(email = "invitee1@example.com"),
                    MockJointLandlordData.createJointLandlordInvitation(email = "invitee2@example.com"),
                ),
            )
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI(signInUrl))

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockInviteeCancellationEmailSender).sendEmail(
            eq("invitee1@example.com"),
            argThat<PropertyDeregistrationInviteeCancellationEmail> {
                this.multiLineAddress == multiLineAddress && this.signInUrl == signInUrl
            },
        )
        verify(mockInviteeCancellationEmailSender).sendEmail(
            eq("invitee2@example.com"),
            argThat<PropertyDeregistrationInviteeCancellationEmail> {
                this.multiLineAddress == multiLineAddress && this.signInUrl == signInUrl
            },
        )
    }

    @Test
    fun `afterStepDataIsAdded sends no invitee cancellation email when there are no pending invitations`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
            .thenReturn(MockLandlordData.createPropertyOwnership())

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockInviteeCancellationEmailSender, never()).sendEmail(any(), any())
    }

    private fun setupStepConfig(): ConfirmStepConfig {
        val stepConfig =
            ConfirmStepConfig(
                mockPropertyOwnershipService,
                mockPropertyDeregistrationService,
                mockJointLandlordInvitationService,
                mockAbsoluteUrlProvider,
                mockConfirmationEmailSender,
                mockInviteeCancellationEmailSender,
            )
        stepConfig.routeSegment = ConfirmStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
