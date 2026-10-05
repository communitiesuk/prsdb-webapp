package uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

@ExtendWith(MockitoExtension::class)
class JoinOrganisationStepConfigTests {
    @Mock
    lateinit var mockInvitationService: OrganisationalLandlordInvitationService

    @Mock
    lateinit var mockState: AcceptInvitationJourneyState

    @Test
    fun `getStepSpecificContent throws when invitation cannot be found for journey`() {
        val journeyId = "test-journey-id"
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationForJourneyIdOrNull(journeyId)).thenReturn(null)

        assertThrows<PrsdbWebException> {
            JoinOrganisationStepConfig(mockInvitationService).getStepSpecificContent(mockState)
        }
    }
}
