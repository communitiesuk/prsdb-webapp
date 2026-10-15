package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import kotlin.test.assertSame

class CompleteInvitationAcceptanceStepConfigTests {
    @Test
    fun `resolveNextDestination deletes the journey before returning the confirmation destination`() {
        // Arrange
        val state = mock<AcceptOrganisationalLandlordUserInvitationJourneyState>()
        val stepConfig = CompleteInvitationAcceptanceStepConfig(mock(), mock())
        val destination = Destination.ExternalUrl(ACCEPT_INVITATION_CONFIRMATION_ROUTE)

        // Act
        val result = stepConfig.resolveNextDestination(state, destination)

        // Assert
        verify(state).deleteJourney()
        assertSame(destination, result)
    }
}
