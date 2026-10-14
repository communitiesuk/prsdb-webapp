package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class AcceptOrganisationalLandlordInvitationCheckAnswersStepConfigTests {
    @Mock
    lateinit var mockInvitationService: OrganisationalLandlordInvitationService

    @Mock
    lateinit var mockState: AcceptOrganisationalLandlordUserInvitationJourneyState

    @Test
    fun `beforeAttemptingToReachStep returns true when a pending invitation is found`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID))
            .thenReturn(mock<OrganisationalLandlordInvitation>())

        assertTrue(
            AcceptOrganisationalLandlordInvitationCheckAnswersStepConfig(mockInvitationService)
                .beforeAttemptingToReachStep(mockState),
        )
    }

    @Test
    fun `beforeAttemptingToReachStep returns false when no pending invitation is found`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID)).thenReturn(null)

        assertFalse(
            AcceptOrganisationalLandlordInvitationCheckAnswersStepConfig(mockInvitationService)
                .beforeAttemptingToReachStep(mockState),
        )
    }

    companion object {
        private const val JOURNEY_ID = "test-journey-id"
    }
}
