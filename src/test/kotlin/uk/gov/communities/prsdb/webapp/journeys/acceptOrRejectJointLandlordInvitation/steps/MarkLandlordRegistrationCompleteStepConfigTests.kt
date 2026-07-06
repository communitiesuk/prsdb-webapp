package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class MarkLandlordRegistrationCompleteStepConfigTests {
    @Mock
    lateinit var mockState: AcceptOrRejectJointLandlordInvitationJourneyState

    @Mock
    lateinit var mockLandlordService: LandlordService

    private val baseUserId = "test-user"

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `afterStepIsReached sets userCompletedLandlordRegistrationThisJourney to true`() {
        // Arrange
        val stepConfig = MarkLandlordRegistrationCompleteStepConfig(mockLandlordService)
        setMockPrincipal(baseUserId)
        val mockLandlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId))
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(mockLandlord)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).userCompletedLandlordRegistrationThisJourney = true
    }

    @Test
    fun `afterStepIsReached saves the registered landlord registration number to state`() {
        // Arrange
        val stepConfig = MarkLandlordRegistrationCompleteStepConfig(mockLandlordService)
        setMockPrincipal(baseUserId)
        val mockLandlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId))
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(mockLandlord)
        val expectedRegNumber = RegistrationNumberDataModel.fromRegistrationNumber(mockLandlord.registrationNumber).toString()

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).registeredLandlordRegistrationNumber = eq(expectedRegNumber)
    }

    private fun setMockPrincipal(name: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
