package uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InOrder
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.CancelLettingAgentDelegationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.CancelLettingAgentDelegationEmailService
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@ExtendWith(MockitoExtension::class)
class RemoveDelegationStepConfigTests {
    @Mock
    lateinit var mockLettingAgentAccessService: LettingAgentAccessService

    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockCancelLettingAgentDelegationEmailService: CancelLettingAgentDelegationEmailService

    @Mock
    lateinit var mockState: CancelLettingAgentDelegationJourneyState

    @Mock
    lateinit var mockPropertyOwnership: PropertyOwnership

    @Mock
    lateinit var mockLettingAgentAccess: LettingAgentAccess

    private fun createStepConfig() =
        RemoveDelegationStepConfig(
            mockLettingAgentAccessService,
            mockPropertyOwnershipService,
            mockCancelLettingAgentDelegationEmailService,
        )

    @Test
    fun `mode is always COMPLETE`() {
        assertEquals(Complete.COMPLETE, createStepConfig().mode(mockState))
    }

    @Test
    fun `afterStepIsReached deletes delegation then sends cancellation emails`() {
        val stepConfig = createStepConfig()

        whenever(mockState.propertyOwnershipId).thenReturn(PROPERTY_OWNERSHIP_ID)
        whenever(mockLettingAgentAccessService.getInvitationByPropertyOwnershipId(PROPERTY_OWNERSHIP_ID)).thenReturn(mockLettingAgentAccess)
        whenever(mockLettingAgentAccess.invitedEmail).thenReturn("agent@example.com")
        whenever(mockPropertyOwnershipService.getPropertyOwnership(PROPERTY_OWNERSHIP_ID)).thenReturn(
            mockPropertyOwnership,
        )

        stepConfig.afterStepIsReached(mockState)

        val inOrder: InOrder = inOrder(mockLettingAgentAccessService, mockCancelLettingAgentDelegationEmailService)
        inOrder.verify(mockLettingAgentAccessService).deleteDelegationByPropertyOwnershipId(PROPERTY_OWNERSHIP_ID)
        inOrder
            .verify(mockCancelLettingAgentDelegationEmailService)
            .sendCancellationEmails(mockPropertyOwnership, "agent@example.com")
    }

    @Test
    fun `afterStepIsReached throws PrsdbWebException when letting agent access is null`() {
        val stepConfig = createStepConfig()

        whenever(mockState.propertyOwnershipId).thenReturn(PROPERTY_OWNERSHIP_ID)
        whenever(mockLettingAgentAccessService.getInvitationByPropertyOwnershipId(PROPERTY_OWNERSHIP_ID)).thenReturn(null)

        assertThrows<PrsdbWebException> {
            stepConfig.afterStepIsReached(mockState)
        }
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination`() {
        val stepConfig = createStepConfig()
        val defaultDestination = Destination.ExternalUrl("/landlord/property-details/$PROPERTY_OWNERSHIP_ID")

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assertEquals(defaultDestination, result)
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 1L
    }
}
