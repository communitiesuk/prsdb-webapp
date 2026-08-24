package uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.CancelLettingAgentDelegationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService

@ExtendWith(MockitoExtension::class)
class RemoveDelegationStepConfigTests {
    @Mock
    lateinit var lettingAgentAccessService: LettingAgentAccessService

    @Mock
    lateinit var mockState: CancelLettingAgentDelegationJourneyState

    @InjectMocks
    lateinit var stepConfig: RemoveDelegationStepConfig

    @Test
    fun `mode is always COMPLETE`() {
        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `afterStepIsReached deletes the letting agent delegation for the property`() {
        whenever(mockState.propertyOwnershipId).thenReturn(1L)

        stepConfig.afterStepIsReached(mockState)

        verify(lettingAgentAccessService).deleteDelegationByPropertyOwnershipId(1L)
    }

    @Test
    fun `resolveNextDestination deletes the journey state`() {
        val defaultDestination = Destination.ExternalUrl("/some-url")

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assertEquals(defaultDestination, result)
    }
}
