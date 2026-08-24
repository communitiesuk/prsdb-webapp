package uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.CancelLettingAgentDelegationJourneyState
import uk.gov.communities.prsdb.webapp.services.CancelLettingAgentDelegationEmailService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class AreYouSureStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockCancelLettingAgentDelegationEmailService: CancelLettingAgentDelegationEmailService

    @Mock
    lateinit var mockJourneyState: CancelLettingAgentDelegationJourneyState

    @Mock
    lateinit var mockPropertyOwnership: PropertyOwnership

    @Mock
    lateinit var mockLettingAgentAccess: LettingAgentAccess

    private fun createStepConfig() =
        AreYouSureStepConfig(mockPropertyOwnershipService, mockCancelLettingAgentDelegationEmailService).apply {
            urlPath = AreYouSureStep.ROUTE_SEGMENT
            validator = AlwaysTrueValidator()
        }

    @Test
    fun `afterStepDataIsAdded sends cancellation emails with the letting agent email`() {
        val stepConfig = createStepConfig()

        whenever(mockJourneyState.propertyOwnershipId).thenReturn(PROPERTY_OWNERSHIP_ID)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(PROPERTY_OWNERSHIP_ID)).thenReturn(mockPropertyOwnership)
        whenever(mockPropertyOwnership.lettingAgentAccess).thenReturn(mockLettingAgentAccess)
        whenever(mockLettingAgentAccess.invitedEmail).thenReturn("agent@example.com")

        stepConfig.afterStepDataIsAdded(mockJourneyState)

        verify(mockCancelLettingAgentDelegationEmailService).sendCancellationEmails(mockPropertyOwnership, "agent@example.com")
    }

    @Test
    fun `afterStepDataIsAdded throws PrsdbWebException when letting agent access is null`() {
        val stepConfig = createStepConfig()

        whenever(mockJourneyState.propertyOwnershipId).thenReturn(PROPERTY_OWNERSHIP_ID)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(PROPERTY_OWNERSHIP_ID)).thenReturn(mockPropertyOwnership)
        whenever(mockPropertyOwnership.lettingAgentAccess).thenReturn(null)

        assertThrows<PrsdbWebException> {
            stepConfig.afterStepDataIsAdded(mockJourneyState)
        }
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination`() {
        val stepConfig = createStepConfig()
        val defaultDestination = Destination.ExternalUrl("/landlord/property-details/$PROPERTY_OWNERSHIP_ID")

        val result = stepConfig.resolveNextDestination(mockJourneyState, defaultDestination)

        verify(mockJourneyState).deleteJourney()
        assertEquals(defaultDestination, result)
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 1L
    }
}
