package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import kotlin.test.assertSame

@ExtendWith(MockitoExtension::class)
class CompleteRemoveOrgUserStepConfigTests {
    @Mock
    private lateinit var mockOrganisationalLandlordUserService: OrganisationalLandlordUserService

    @Mock
    private lateinit var mockState: ChangeOrgTeamMemberAccessLevelJourneyState

    private fun createStepConfig() = CompleteRemoveOrgUserStepConfig(mockOrganisationalLandlordUserService)

    @Test
    fun `afterStepIsReached removes the target membership`() {
        whenever(mockState.otherOrganisationalLandlordUserId).thenReturn(42L)

        createStepConfig().afterStepIsReached(mockState)

        verify(mockOrganisationalLandlordUserService).removeOrganisationalLandlordUser(42L)
    }

    @Test
    fun `resolveNextDestination deletes the journey before returning the default destination`() {
        // Arrange
        val destination = Destination.ExternalUrl(TeamMembersController.TEAM_MEMBERS_ROUTE)

        // Act
        val result = createStepConfig().resolveNextDestination(mockState, destination)

        // Assert
        verify(mockState).deleteJourney()
        assertSame(destination, result)
    }
}
