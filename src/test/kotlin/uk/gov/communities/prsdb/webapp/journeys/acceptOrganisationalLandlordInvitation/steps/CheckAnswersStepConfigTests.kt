package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

// TODO: PDJB-1774 - Rename to make more specific
@ExtendWith(MockitoExtension::class)
class CheckAnswersStepConfigTests {
    @Mock
    private lateinit var invitationService: OrganisationalLandlordInvitationService

    @Mock
    private lateinit var state: AcceptOrganisationalLandlordUserInvitationJourneyState

    @Test
    fun `afterStepDataIsAdded does not accept invitation`() {
        CheckAnswersStepConfig(invitationService).afterStepDataIsAdded(state)

        verify(invitationService, never()).acceptInvitation(any(), any(), any(), any())
        verify(invitationService, never()).clearJourneyIdInvitationTokenPairsForTokenFromSession(any())
    }
}
