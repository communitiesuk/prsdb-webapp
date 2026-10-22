package uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.stepConfig

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.CancelOrganisationalLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

@ExtendWith(MockitoExtension::class)
class CancelInvitationStepConfigTests {
    @Mock
    lateinit var mockInvitationService: OrganisationalLandlordInvitationService

    @Mock
    lateinit var mockState: CancelOrganisationalLandlordInvitationJourneyState

    private fun setupStepConfig() = CancelInvitationStepConfig(mockInvitationService)

    @Test
    fun `afterStepIsReached cancels the invitation`() {
        whenever(mockState.invitationId).thenReturn(1L)

        setupStepConfig().afterStepIsReached(mockState)

        verify(mockInvitationService).cancelInvitationIfAuthorized(1L)
    }

    @Test
    fun `resolveNextDestination deletes the journey`() {
        setupStepConfig().resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))

        verify(mockState).deleteJourney()
    }
}
