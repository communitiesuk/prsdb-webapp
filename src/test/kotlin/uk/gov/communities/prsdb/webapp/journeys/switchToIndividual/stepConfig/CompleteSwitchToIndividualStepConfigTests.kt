package uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.SWITCHED_TO_INDIVIDUAL_PROPERTY_ID
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.SwitchToIndividualJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationCancellationInviteeEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.SwitchToIndividualConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class CompleteSwitchToIndividualStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockJointLandlordInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockInviteCancellationEmailSender: EmailNotificationService<JointLandlordInvitationCancellationInviteeEmail>

    @Mock
    lateinit var mockSwitchToIndividualConfirmationEmailSender: EmailNotificationService<SwitchToIndividualConfirmationEmail>

    @Mock
    lateinit var mockSession: HttpSession

    @Mock
    lateinit var mockState: SwitchToIndividualJourneyState

    private fun setupStepConfig(): CompleteSwitchToIndividualStepConfig =
        CompleteSwitchToIndividualStepConfig(
            mockPropertyOwnershipService,
            mockJointLandlordInvitationService,
            mockInviteCancellationEmailSender,
            mockSwitchToIndividualConfirmationEmailSender,
            mockSession,
        )

    @Test
    fun `mode returns COMPLETE`() {
        val stepConfig = setupStepConfig()

        val result = stepConfig.mode(mockState)

        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `afterStepIsReached cancels all pending invitations`() {
        val stepConfig = setupStepConfig()
        val propertyOwnershipId = 1L
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        val invitation1 = MockJointLandlordData.createJointLandlordInvitation()
        val invitation2 = MockJointLandlordData.createJointLandlordInvitation()
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockJointLandlordInvitationService.getPendingInvitations(propertyOwnership))
            .thenReturn(listOf(invitation1, invitation2))

        stepConfig.afterStepIsReached(mockState)

        verify(mockJointLandlordInvitationService).cancelInvitations(listOf(invitation1, invitation2))
    }

    @Test
    fun `afterStepIsReached marks property as not joint landlord`() {
        val stepConfig = setupStepConfig()
        val propertyOwnershipId = 1L
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockJointLandlordInvitationService.getPendingInvitations(propertyOwnership))
            .thenReturn(emptyList())

        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyOwnershipService).markAsNotJointLandlord(propertyOwnership)
    }

    @Test
    fun `afterStepIsReached stores property ownership id in session`() {
        val stepConfig = setupStepConfig()
        val propertyOwnershipId = 1L
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockJointLandlordInvitationService.getPendingInvitations(propertyOwnership))
            .thenReturn(emptyList())

        stepConfig.afterStepIsReached(mockState)

        verify(mockSession).setAttribute(SWITCHED_TO_INDIVIDUAL_PROPERTY_ID, propertyOwnershipId)
    }

    @Test
    fun `afterStepIsReached throws if property has more than one landlord`() {
        val stepConfig = setupStepConfig()
        val propertyOwnershipId = 1L
        val secondLandlord = MockLandlordData.createLandlord()
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                otherLandlords = mutableSetOf(secondLandlord),
            )
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        assertThrows<PrsdbWebException> {
            stepConfig.afterStepIsReached(mockState)
        }
    }

    @Test
    fun `afterStepIsReached sends cancellation email to each invitee`() {
        val stepConfig = setupStepConfig()
        val propertyOwnershipId = 1L
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        val invitation1 = MockJointLandlordData.createJointLandlordInvitation(email = "invitee1@example.com")
        val invitation2 = MockJointLandlordData.createJointLandlordInvitation(email = "invitee2@example.com")
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockJointLandlordInvitationService.getPendingInvitations(propertyOwnership))
            .thenReturn(listOf(invitation1, invitation2))

        stepConfig.afterStepIsReached(mockState)

        verify(
            mockInviteCancellationEmailSender,
        ).sendEmail(eq("invitee1@example.com"), any<JointLandlordInvitationCancellationInviteeEmail>())
        verify(
            mockInviteCancellationEmailSender,
        ).sendEmail(eq("invitee2@example.com"), any<JointLandlordInvitationCancellationInviteeEmail>())
    }

    @Test
    fun `afterStepIsReached sends confirmation email to the landlord`() {
        val stepConfig = setupStepConfig()
        val propertyOwnershipId = 1L
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockJointLandlordInvitationService.getPendingInvitations(propertyOwnership))
            .thenReturn(emptyList())

        stepConfig.afterStepIsReached(mockState)

        verify(mockSwitchToIndividualConfirmationEmailSender).sendEmail(
            eq(propertyOwnership.primaryLandlord.email),
            any<SwitchToIndividualConfirmationEmail>(),
        )
    }
}
