package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationRejectionEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import java.net.URI

@ExtendWith(MockitoExtension::class)
class DeleteInvitationAndTokenStepConfigTests {
    @Mock
    lateinit var mockInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockInvitationRepository: JointLandlordInvitationRepository

    @Mock
    lateinit var mockRejectionEmailSender: EmailNotificationService<JointLandlordInvitationRejectionEmail>

    @Mock
    lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    lateinit var mockState: AcceptOrRejectJointLandlordInvitationJourneyState

    private val journeyId = "test-journey-id"
    private val token = "aaaabbbb-cccc-dddd-eeee-ffff00001111"

    @Test
    fun `afterStepIsReached deletes invitation and clears session tokens`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val invitation = mock<JointLandlordInvitation>()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockInvitationService.getInvitationForJourney(journeyId)).thenReturn(invitation)

        val mockAcceptOrRejectStep = mock<AcceptOrRejectStep>()
        whenever(mockAcceptOrRejectStep.outcome).thenReturn(YesOrNo.YES)
        whenever(mockState.acceptOrRejectStep).thenReturn(mockAcceptOrRejectStep)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockInvitationRepository).delete(invitation)
        verify(mockInvitationService).clearJourneyIdInvitationTokenPairsForTokenFromSession(token)
    }

    @Test
    fun `afterStepIsReached sends rejection email to all landlords when invitation is rejected`() {
        // Arrange
        val stepConfig = setupStepConfig()

        val landlord1 = mock<Landlord>()
        whenever(landlord1.name).thenReturn("Lois Lane")
        whenever(landlord1.email).thenReturn("lois@example.com")

        val landlord2 = mock<Landlord>()
        whenever(landlord2.name).thenReturn("Clark Kent")
        whenever(landlord2.email).thenReturn("clark@example.com")

        val mockAddress = mock<Address>()
        whenever(mockAddress.singleLineAddress).thenReturn("Flat 1, 11 Elm Drive, London, NW8 2DK")

        val mockPropertyOwnership = mock<PropertyOwnership>()
        whenever(mockPropertyOwnership.address).thenReturn(mockAddress)
        whenever(mockPropertyOwnership.landlords).thenReturn(mutableSetOf(landlord1, landlord2))
        whenever(mockPropertyOwnership.id).thenReturn(42L)

        val invitingLandlord = mock<Landlord>()
        whenever(invitingLandlord.name).thenReturn("Lois Lane")

        val invitation = mock<JointLandlordInvitation>()
        whenever(invitation.registeredOwnership).thenReturn(mockPropertyOwnership)
        whenever(invitation.invitingLandlord).thenReturn(invitingLandlord)
        whenever(invitation.invitedEmail).thenReturn("invitee@example.com")

        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationForJourney(journeyId)).thenReturn(invitation)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(42L)).thenReturn(URI("http://localhost/property/42"))

        val mockAcceptOrRejectStep = mock<AcceptOrRejectStep>()
        whenever(mockAcceptOrRejectStep.outcome).thenReturn(YesOrNo.NO)
        whenever(mockState.acceptOrRejectStep).thenReturn(mockAcceptOrRejectStep)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockRejectionEmailSender).sendEmail(
            eq("lois@example.com"),
            eq(
                JointLandlordInvitationRejectionEmail(
                    recipientName = "Lois Lane",
                    inviteeEmail = "invitee@example.com",
                    propertyAddress = "Flat 1, 11 Elm Drive, London, NW8 2DK",
                    propertyRecordUrl = "http://localhost/property/42",
                ),
            ),
        )
        verify(mockRejectionEmailSender).sendEmail(
            eq("clark@example.com"),
            eq(
                JointLandlordInvitationRejectionEmail(
                    recipientName = "Clark Kent",
                    inviteeEmail = "invitee@example.com",
                    propertyAddress = "Flat 1, 11 Elm Drive, London, NW8 2DK",
                    propertyRecordUrl = "http://localhost/property/42",
                ),
            ),
        )
    }

    @Test
    fun `afterStepIsReached stores rejection confirmation data in session when invitation is rejected`() {
        // Arrange
        val stepConfig = setupStepConfig()

        val mockAddress = mock<Address>()
        whenever(mockAddress.singleLineAddress).thenReturn("Flat 1, 11 Elm Drive, London, NW8 2DK")

        val landlord = mock<Landlord>()
        whenever(landlord.name).thenReturn("Lois Lane")
        whenever(landlord.email).thenReturn("lois@example.com")

        val mockPropertyOwnership = mock<PropertyOwnership>()
        whenever(mockPropertyOwnership.address).thenReturn(mockAddress)
        whenever(mockPropertyOwnership.landlords).thenReturn(mutableSetOf(landlord))
        whenever(mockPropertyOwnership.id).thenReturn(42L)

        val invitingLandlord = mock<Landlord>()
        whenever(invitingLandlord.name).thenReturn("Lois Lane")

        val invitation = mock<JointLandlordInvitation>()
        whenever(invitation.registeredOwnership).thenReturn(mockPropertyOwnership)
        whenever(invitation.invitingLandlord).thenReturn(invitingLandlord)
        whenever(invitation.invitedEmail).thenReturn("invitee@example.com")

        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationForJourney(journeyId)).thenReturn(invitation)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(42L)).thenReturn(URI("http://localhost/property/42"))

        val mockAcceptOrRejectStep = mock<AcceptOrRejectStep>()
        whenever(mockAcceptOrRejectStep.outcome).thenReturn(YesOrNo.NO)
        whenever(mockState.acceptOrRejectStep).thenReturn(mockAcceptOrRejectStep)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockInvitationService).addRejectionConfirmationDataToSession(
            "Flat 1, 11 Elm Drive, London, NW8 2DK",
        )
    }

    @Test
    fun `afterStepIsReached does not send rejection email when invitation is accepted`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val invitation = mock<JointLandlordInvitation>()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockInvitationService.getInvitationForJourney(journeyId)).thenReturn(invitation)

        val mockAcceptOrRejectStep = mock<AcceptOrRejectStep>()
        whenever(mockAcceptOrRejectStep.outcome).thenReturn(YesOrNo.YES)
        whenever(mockState.acceptOrRejectStep).thenReturn(mockAcceptOrRejectStep)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockRejectionEmailSender, never()).sendEmail(any(), any())
        verify(mockInvitationService, never()).addRejectionConfirmationDataToSession(any())
    }

    @Test
    fun `resolveNextDestination deletes journey and returns default destination`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val defaultDestination = Destination.ExternalUrl("/some-url")

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        verify(mockState).deleteJourney()
        assert(result == defaultDestination)
    }

    private fun setupStepConfig() =
        DeleteInvitationAndTokenStepConfig(
            mockInvitationService,
            mockInvitationRepository,
            mockRejectionEmailSender,
            mockAbsoluteUrlProvider,
        )
}
