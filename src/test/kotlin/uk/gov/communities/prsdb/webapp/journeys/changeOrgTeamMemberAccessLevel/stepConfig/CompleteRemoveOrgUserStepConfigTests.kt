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
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
    fun `resolveNextDestination clears the journey and returns to team members`() {
        val defaultDestination = Destination.Nowhere()

        val result = createStepConfig().resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assertEquals(
            TeamMembersController.TEAM_MEMBERS_ROUTE,
            assertIs<Destination.ExternalUrl>(result).externalUrl,
        )
    }
}
