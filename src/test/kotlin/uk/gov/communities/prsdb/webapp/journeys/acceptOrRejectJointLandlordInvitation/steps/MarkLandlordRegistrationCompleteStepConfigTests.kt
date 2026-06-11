package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState

@ExtendWith(MockitoExtension::class)
class MarkLandlordRegistrationCompleteStepConfigTests {
    @Mock
    lateinit var mockState: AcceptOrRejectJointLandlordInvitationJourneyState

    @Test
    fun `afterStepIsReached sets userCompletedLandlordRegistrationThisJourney to true`() {
        // Arrange
        val stepConfig = MarkLandlordRegistrationCompleteStepConfig()

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).userCompletedLandlordRegistrationThisJourney = true
    }
}
