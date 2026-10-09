package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class JoinOrganisationStepConfigTests {
    @Mock
    lateinit var mockState: AcceptOrganisationalLandlordUserInvitationJourneyState

    @Test
    fun `getStepSpecificContent returns the organisation name cached in journey state`() {
        val organisationName = "Test Organisation"
        whenever(mockState.organisationName).thenReturn(organisationName)

        val content = JoinOrganisationStepConfig().getStepSpecificContent(mockState)

        assertEquals(organisationName, content["organisationName"])
    }

    @Test
    fun `getStepSpecificContent throws when no organisation name has been cached in journey state`() {
        whenever(mockState.organisationName).thenReturn(null)

        assertThrows<PrsdbWebException> {
            JoinOrganisationStepConfig().getStepSpecificContent(mockState)
        }
    }
}
