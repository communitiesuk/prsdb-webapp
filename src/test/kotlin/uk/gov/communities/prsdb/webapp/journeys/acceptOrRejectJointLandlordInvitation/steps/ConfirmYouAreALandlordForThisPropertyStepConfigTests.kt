package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class ConfirmYouAreALandlordForThisPropertyStepConfigTests {
    @Mock
    lateinit var mockInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockLandlordService: LandlordService

    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockState: AcceptOrRejectJointLandlordInvitationJourneyState

    private val journeyId = "test-journey-id"
    private val token = "aaaabbbb-cccc-dddd-eeee-ffff00001111"
    private val baseUserId = "test-user"

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getStepSpecificContent returns property address from invitation`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val invitation =
            MockJointLandlordData.createJointLandlordInvitation(
                propertyOwnership =
                    MockLandlordData.createPropertyOwnership(
                        address = MockLandlordData.createAddress("1 Fake Street, Faketown, FK1 2AB"),
                    ),
            )
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationForJourney(journeyId)).thenReturn(invitation)
        whenever(mockState.registeredLandlordRegistrationNumber).thenReturn(null)

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(listOf("1 Fake Street", "Faketown", "FK1 2AB"), content["propertyAddress"])
    }

    @Test
    fun `getStepSpecificContent shows success banner when user completed landlord registration this journey`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val invitation = MockJointLandlordData.createJointLandlordInvitation()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationForJourney(journeyId)).thenReturn(invitation)
        whenever(mockState.registeredLandlordRegistrationNumber).thenReturn("P-1234-5678")

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(true, content["showSuccessBanner"])
        assertEquals("P-1234-5678", content["registrationNumber"])
    }

    @Test
    fun `getStepSpecificContent hides success banner when user was already registered`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val invitation = MockJointLandlordData.createJointLandlordInvitation()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationForJourney(journeyId)).thenReturn(invitation)
        whenever(mockState.registeredLandlordRegistrationNumber).thenReturn(null)

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(false, content["showSuccessBanner"])
        assertNull(content["registrationNumber"])
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `afterStepDataIsAdded sets tokenIsValid to the result of getTokenIsValid`(tokenIsValid: Boolean) {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockInvitationService.getTokenIsValid(token)).thenReturn(tokenIsValid)
        whenever(mockState.tokenIsValid).thenReturn(tokenIsValid)
        if (tokenIsValid) {
            setMockPrincipal(baseUserId)
            val mockLandlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId))
            whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(mockLandlord)
            val invitation = MockJointLandlordData.createJointLandlordInvitation()
            whenever(mockInvitationService.getInvitationFromToken(token)).thenReturn(invitation)
        }

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockState).tokenIsValid = tokenIsValid
    }

    @Test
    fun `afterStepDataIsAdded adds landlord to property ownership when token is valid`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 42)
        val invitation = MockJointLandlordData.createJointLandlordInvitation(propertyOwnership = propertyOwnership)
        val mockLandlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId))
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockInvitationService.getTokenIsValid(token)).thenReturn(true)
        whenever(mockState.tokenIsValid).thenReturn(true)
        setMockPrincipal(baseUserId)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(mockLandlord)
        whenever(mockInvitationService.getInvitationFromToken(token)).thenReturn(invitation)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyOwnershipService).addLandlordToPropertyOwnership(propertyOwnership.id, mockLandlord)
    }

    private fun setupStepConfig() =
        ConfirmYouAreALandlordForThisPropertyStepConfig(
            mockInvitationService,
            mockLandlordService,
            mockPropertyOwnershipService,
        )

    private fun setMockPrincipal(name: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
