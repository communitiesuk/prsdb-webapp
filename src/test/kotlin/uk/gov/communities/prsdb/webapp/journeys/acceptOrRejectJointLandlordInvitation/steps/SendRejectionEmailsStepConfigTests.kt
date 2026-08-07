package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationRejectionEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class SendRejectionEmailsStepConfigTests {
    @Mock
    lateinit var mockInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockRejectionEmailSender: EmailNotificationService<JointLandlordInvitationRejectionEmail>

    @Mock
    lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    lateinit var mockState: AcceptOrRejectJointLandlordInvitationJourneyState

    private val journeyId = "test-journey-id"

    @Test
    fun `afterStepIsReached sends rejection email to all landlords`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val address = MockLandlordData.createAddress("Flat 1, 11 Elm Drive, London, NW8 2DK")
        val propertyAddress = address.toMultiLineAddress()
        val individualLandlord =
            MockLandlordData.createIndividualLandlord(
                name = "Lois Lane",
                email = "lois@example.com",
            )
        val organisationLandlord =
            MockLandlordData.createOrgLandlord(
                name = "Clark Properties Ltd",
                registrantName = "Clark Kent",
                registrantEmail = "clark@example.com",
            )
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = 42L,
                address = address,
                landlords = mutableSetOf(individualLandlord, organisationLandlord),
            )
        val invitation =
            MockJointLandlordData.createJointLandlordInvitation(
                email = "invitee@example.com",
                propertyOwnership = propertyOwnership,
            )

        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationForJourney(journeyId)).thenReturn(invitation)
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(42L)).thenReturn(URI("http://localhost/property/42"))

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockRejectionEmailSender).sendEmail(
            eq(individualLandlord.email),
            eq(
                JointLandlordInvitationRejectionEmail(
                    recipientName = individualLandlord.name,
                    inviteeEmail = "invitee@example.com",
                    propertyAddress = propertyAddress,
                    propertyRecordUrl = "http://localhost/property/42",
                ),
            ),
        )
        verify(mockRejectionEmailSender).sendEmail(
            eq(organisationLandlord.email),
            eq(
                JointLandlordInvitationRejectionEmail(
                    recipientName = organisationLandlord.name,
                    inviteeEmail = "invitee@example.com",
                    propertyAddress = propertyAddress,
                    propertyRecordUrl = "http://localhost/property/42",
                ),
            ),
        )
    }

    @Test
    fun `afterStepIsReached stores rejection property address in session`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val address = MockLandlordData.createAddress("Flat 1, 11 Elm Drive, London, NW8 2DK")
        val propertyAddress = address.toMultiLineAddress()
        val landlord = MockLandlordData.createOrgLandlord()
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = 42L,
                address = address,
                landlords = mutableSetOf(landlord),
            )
        val invitation = MockJointLandlordData.createJointLandlordInvitation(propertyOwnership = propertyOwnership)

        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationForJourney(journeyId)).thenReturn(invitation)
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(42L)).thenReturn(URI("http://localhost/property/42"))

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockInvitationService).addRejectedPropertyAddressToSession(propertyAddress)
    }

    private fun setupStepConfig() =
        SendRejectionEmailsStepConfig(
            mockInvitationService,
            mockRejectionEmailSender,
            mockAbsoluteUrlProvider,
        )
}
