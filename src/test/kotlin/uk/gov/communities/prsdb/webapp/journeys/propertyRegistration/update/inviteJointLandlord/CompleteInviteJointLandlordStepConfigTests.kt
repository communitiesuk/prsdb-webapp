package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
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

    @Nested
    inner class YesJointStepConfigTests {
        @Test
        fun `afterStepIsReached marks as joint landlord and sends invitation emails`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
            val stepConfig =
                CompleteInviteJointLandlordYesJointStepConfig(mockJointLandlordInvitationService, mockPropertyOwnershipService)
            whenever(mockState.propertyId).thenReturn(propertyId)
            whenever(mockState.invitedJointLandlords).thenReturn(invitedEmails)
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)

            stepConfig.afterStepIsReached(mockState)

            verify(mockPropertyOwnershipService).markAsJointLandlord(propertyId)
            verify(mockJointLandlordInvitationService).sendInvitationEmails(
                jointLandlordEmails = eq(invitedEmails),
                propertyOwnership = eq(propertyOwnership),
                invitingLandlord = eq(propertyOwnership.primaryLandlord),
            )
        }

        @Test
        fun `resolveNextDestination deletes the journey and returns the default destination`() {
            val stepConfig =
                CompleteInviteJointLandlordYesJointStepConfig(mockJointLandlordInvitationService, mockPropertyOwnershipService)
            val defaultDestination = Destination.ExternalUrl("/redirect")

            val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

            verify(mockState).deleteJourney()
            assertEquals(defaultDestination, result)
        }

        @Test
        fun `mode always returns COMPLETE`() {
            val stepConfig =
                CompleteInviteJointLandlordYesJointStepConfig(mockJointLandlordInvitationService, mockPropertyOwnershipService)

            assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
        }
    }

    @Nested
    inner class NoJointStepConfigTests {
        @Test
        fun `resolveNextDestination deletes the journey and returns the default destination`() {
            val stepConfig = CompleteInviteJointLandlordNoJointStepConfig()
            val defaultDestination = Destination.ExternalUrl("/redirect")

            val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

            verify(mockState).deleteJourney()
            assertEquals(defaultDestination, result)
        }

        @Test
        fun `mode always returns COMPLETE`() {
            val stepConfig = CompleteInviteJointLandlordNoJointStepConfig()

            assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
        }
    }
}
