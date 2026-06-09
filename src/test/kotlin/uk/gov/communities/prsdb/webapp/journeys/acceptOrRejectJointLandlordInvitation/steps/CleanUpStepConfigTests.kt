package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService

@ExtendWith(MockitoExtension::class)
class CleanUpStepConfigTests {
    @Mock
    lateinit var mockInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockInvitationRepository: JointLandlordInvitationRepository

    @Mock
    lateinit var mockState: AcceptOrRejectJointLandlordInvitationJourneyState

    private val journeyId = "test-journey-id"
    private val token = "aaaabbbb-cccc-dddd-eeee-ffff00001111"

    @Test
    fun `afterStepIsReached deletes invitation and clears session tokens`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val invitation = mock<JointLandlordInvitation>()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockInvitationService.getInvitationForJourney(journeyId)).thenReturn(invitation)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockInvitationRepository).delete(invitation)
        verify(mockInvitationService).clearJourneyIdInvitationTokenPairsForTokenFromSession(token)
    }

    @Test
    fun `resolveNextDestination deletes journey and returns default destination`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val defaultDestination = Destination.ExternalUrl("/some-url")

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        verify(mockState).deleteJourney()
        assert(result == defaultDestination)
    }

    private fun setupStepConfig() = DeleteInvitationAndTokenStepConfig(mockInvitationService, mockInvitationRepository)
}
