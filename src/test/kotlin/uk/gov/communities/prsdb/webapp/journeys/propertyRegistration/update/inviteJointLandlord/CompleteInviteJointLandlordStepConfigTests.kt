package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class CompleteInviteJointLandlordStepConfigTests {
    @Mock
    private lateinit var mockJointLandlordInvitationService: JointLandlordInvitationService

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockState: InviteJointLandlordJourneyState

    private val propertyId = 123L
    private val invitedEmails = listOf("first@example.com", "second@example.com")

    @Test
    fun `afterStepIsReached marks property as joint landlord and sends invitation emails when invites are present`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val stepConfig = CompleteInviteJointLandlordStepConfig(mockJointLandlordInvitationService, mockPropertyOwnershipService)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.invitedJointLandlords).thenReturn(invitedEmails)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)

        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyOwnershipService).markAsJointLandlord(eq(propertyOwnership))
        verify(mockJointLandlordInvitationService).sendInvitationEmails(
            jointLandlordEmails = eq(invitedEmails),
            propertyOwnership = eq(propertyOwnership),
            invitingLandlord = eq(propertyOwnership.primaryLandlord),
        )
    }

    @Test
    fun `afterStepIsReached does nothing when no invites are present`() {
        val stepConfig = CompleteInviteJointLandlordStepConfig(mockJointLandlordInvitationService, mockPropertyOwnershipService)
        whenever(mockState.invitedJointLandlords).thenReturn(emptyList())

        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyOwnershipService, never()).markAsJointLandlord(any())
        verify(mockJointLandlordInvitationService, never()).sendInvitationEmails(
            jointLandlordEmails = any(),
            propertyOwnership = any(),
            invitingLandlord = any(),
        )
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination`() {
        val stepConfig = CompleteInviteJointLandlordStepConfig(mockJointLandlordInvitationService, mockPropertyOwnershipService)
        val defaultDestination = Destination.ExternalUrl("/redirect")

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assertEquals(defaultDestination, result)
    }

    @Test
    fun `mode always returns COMPLETE`() {
        val stepConfig = CompleteInviteJointLandlordStepConfig(mockJointLandlordInvitationService, mockPropertyOwnershipService)

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }
}
