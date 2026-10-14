package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService

@ExtendWith(MockitoExtension::class)
class CompleteChangeAccessLevelStepConfigTests {
    @Mock
    private lateinit var mockOrganisationalLandlordUserService: OrganisationalLandlordUserService

    @Mock
    private lateinit var mockState: ChangeOrgTeamMemberAccessLevelJourneyState

    private lateinit var stepConfig: CompleteChangeAccessLevelStepConfig

    private val otherOrganisationalLandlordUserId = 42L

    @BeforeEach
    fun setUp() {
        stepConfig = CompleteChangeAccessLevelStepConfig(mockOrganisationalLandlordUserService)
    }

    @Test
    fun `mode returns COMPLETE`() {
        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `afterStepIsReached updates the member's role to the selected role`() {
        // Arrange
        whenever(mockState.otherOrganisationalLandlordUserId).thenReturn(otherOrganisationalLandlordUserId)
        whenever(mockState.selectedRole).thenReturn(OrganisationalLandlordUserRole.EDITOR)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockOrganisationalLandlordUserService).updateRole(otherOrganisationalLandlordUserId, OrganisationalLandlordUserRole.EDITOR)
    }

    @Test
    fun `resolveNextDestination deletes the journey`() {
        // Act
        stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))

        // Assert
        verify(mockState).deleteJourney()
    }
}
