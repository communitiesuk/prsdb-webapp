package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class JoinOrganisationStepConfigTests {
    @Mock
    lateinit var mockInvitationService: OrganisationalLandlordInvitationService

    @Mock
    lateinit var mockState: AcceptOrganisationalLandlordUserInvitationJourneyState

    @Test
    fun `getStepSpecificContent returns the organisation name from the pending invitation`() {
        val organisationName = "Test Organisation"
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID))
            .thenReturn(
                MockLandlordData.createOrganisationalLandlordInvitation(
                    organisationalLandlord = MockLandlordData.createOrgLandlord(name = organisationName),
                ),
            )

        val content = JoinOrganisationStepConfig(mockInvitationService).getStepSpecificContent(mockState)

        assertEquals(organisationName, content["organisationName"])
    }

    @Test
    fun `getStepSpecificContent throws when there is no pending invitation for the journey`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID)).thenReturn(null)

        assertThrows<PrsdbWebException> {
            JoinOrganisationStepConfig(mockInvitationService).getStepSpecificContent(mockState)
        }
    }

    @Test
    fun `beforeAttemptingToReachStep returns true when a pending invitation is found`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID))
            .thenReturn(mock<OrganisationalLandlordInvitation>())

        assertTrue(JoinOrganisationStepConfig(mockInvitationService).beforeAttemptingToReachStep(mockState))
    }

    @Test
    fun `beforeAttemptingToReachStep returns false when no pending invitation is found`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getPendingInvitationForJourneyIdOrNull(JOURNEY_ID)).thenReturn(null)

        assertFalse(JoinOrganisationStepConfig(mockInvitationService).beforeAttemptingToReachStep(mockState))
    }

    companion object {
        private const val JOURNEY_ID = "test-journey-id"
    }
}
