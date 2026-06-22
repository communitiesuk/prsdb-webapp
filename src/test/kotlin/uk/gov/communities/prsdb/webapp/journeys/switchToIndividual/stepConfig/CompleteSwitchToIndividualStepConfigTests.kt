package uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.SWITCHED_TO_INDIVIDUAL_PROPERTY_ID
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.SwitchToIndividualJourneyState
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
    lateinit var mockSession: HttpSession

    @Mock
    lateinit var mockState: SwitchToIndividualJourneyState

    @InjectMocks
    lateinit var stepConfig: CompleteSwitchToIndividualStepConfig

    @Test
    fun `mode returns COMPLETE`() {
        val result = stepConfig.mode(mockState)

        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `afterStepIsReached cancels all pending invitations`() {
        val propertyOwnershipId = 1L
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        val invitation1 = MockJointLandlordData.createJointLandlordInvitation()
        val invitation2 = MockJointLandlordData.createJointLandlordInvitation()
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockJointLandlordInvitationService.getPendingInvitations(propertyOwnership))
            .thenReturn(listOf(invitation1, invitation2))

        stepConfig.afterStepIsReached(mockState)

        verify(mockJointLandlordInvitationService).cancelInvitation(invitation1)
        verify(mockJointLandlordInvitationService).cancelInvitation(invitation2)
    }

    @Test
    fun `afterStepIsReached marks property as not joint landlord`() {
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
}
