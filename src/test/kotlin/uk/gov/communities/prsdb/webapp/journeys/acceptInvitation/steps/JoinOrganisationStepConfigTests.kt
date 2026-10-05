package uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class JoinOrganisationStepConfigTests {
    @Mock
    lateinit var mockInvitationService: OrganisationalLandlordInvitationService

    @Mock
    lateinit var mockState: AcceptInvitationJourneyState

    @Test
    fun `getStepSpecificContent returns organisation name from invitation`() {
        val journeyId = "test-journey-id"
        val organisationName = "Test Organisation"
        val invitation = mock<OrganisationalLandlordInvitation>()
        val organisationalLandlord = mock<OrganisationalLandlord>()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationForJourneyIdOrNull(journeyId)).thenReturn(invitation)
        whenever(invitation.organisationalLandlord).thenReturn(organisationalLandlord)
        whenever(organisationalLandlord.name).thenReturn(organisationName)

        val content = JoinOrganisationStepConfig(mockInvitationService).getStepSpecificContent(mockState)

        assertEquals(organisationName, content["organisationName"])
    }

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
