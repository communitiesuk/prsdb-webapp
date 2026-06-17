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
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationAcceptedEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationAcceptedOtherLandlordEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class ConfirmYouAreALandlordForThisPropertyStepConfigTests {
    @Mock
    lateinit var mockInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockLandlordService: LandlordService

    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    lateinit var mockAcceptedEmailSender: EmailNotificationService<JointLandlordInvitationAcceptedEmail>

    @Mock
    lateinit var mockOtherLandlordEmailSender: EmailNotificationService<JointLandlordInvitationAcceptedOtherLandlordEmail>

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
        setupTokenValidation(tokenIsValid)
        if (tokenIsValid) {
            setupValidTokenWithLandlord()
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
        val mockLandlord = setupValidTokenWithLandlord(invitation)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyOwnershipService).addLandlordToPropertyOwnership(propertyOwnership.id, mockLandlord)
    }

    @Test
    fun `afterStepDataIsAdded stores accepted property in session when token is valid`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val invitation = MockJointLandlordData.createJointLandlordInvitation()
        setupValidTokenWithLandlord(invitation)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockInvitationService).storeLastAcceptedPropertyInSession(
            invitation.registeredOwnership.address.toMultiLineAddress(),
            invitation.registeredOwnership.id,
        )
    }

    @Test
    fun `afterStepDataIsAdded sends accepted email to accepting landlord when token is valid`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 42)
        val invitation = MockJointLandlordData.createJointLandlordInvitation(propertyOwnership = propertyOwnership)
        val landlord = setupValidTokenWithLandlord(invitation)
        val expectedPropertyUrl = "https://example.com/property"

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        val emailCaptor = argumentCaptor<JointLandlordInvitationAcceptedEmail>()
        verify(mockAcceptedEmailSender).sendEmail(eq(landlord.email), emailCaptor.capture())
        val email = emailCaptor.firstValue
        assertEquals(landlord.name, email.recipientName)
        assertEquals(propertyOwnership.address.toMultiLineAddress(), email.propertyAddress)
        assertEquals(expectedPropertyUrl, email.propertyRecordUrl)
        assertEquals(
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
            email.propertyRegistrationNumber,
        )
    }

    @Test
    fun `afterStepDataIsAdded sends other landlord email to existing landlords when token is valid`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val acceptingLandlord =
            MockLandlordData.createLandlord(name = "Accepting Landlord", baseUser = MockLandlordData.createPrsdbUser(baseUserId))
        val otherLandlord =
            MockLandlordData.createLandlord(name = "Other Landlord", baseUser = MockLandlordData.createPrsdbUser("other-user"))
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(primaryLandlord = acceptingLandlord, otherLandlords = mutableSetOf(otherLandlord))
        setupValidTokenWithLandlordAndOwnership(acceptingLandlord, propertyOwnership)
        val expectedPropertyUrl = "https://example.com/property"

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        val emailCaptor = argumentCaptor<JointLandlordInvitationAcceptedOtherLandlordEmail>()
        verify(mockOtherLandlordEmailSender).sendEmail(eq(otherLandlord.email), emailCaptor.capture())
        val email = emailCaptor.firstValue
        assertEquals(otherLandlord.name, email.recipientName)
        assertEquals(acceptingLandlord.name, email.inviteeName)
        assertEquals(propertyOwnership.address.toMultiLineAddress(), email.propertyAddress)
        assertEquals(expectedPropertyUrl, email.propertyRecordUrl)
    }

    @Test
    fun `afterStepDataIsAdded does not send emails when token is invalid`() {
        // Arrange
        val stepConfig = setupStepConfig()
        setupTokenValidation(false)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verifyNoInteractions(mockAcceptedEmailSender)
        verifyNoInteractions(mockOtherLandlordEmailSender)
    }

    @Test
    fun `afterStepDataIsAdded does not send other landlord email to accepting landlord`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val acceptingLandlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId))
        setupValidTokenWithLandlordAndOwnership(
            acceptingLandlord,
            MockLandlordData.createPropertyOwnership(primaryLandlord = acceptingLandlord),
        )

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verifyNoInteractions(mockOtherLandlordEmailSender)
    }

    private fun setupStepConfig() =
        ConfirmYouAreALandlordForThisPropertyStepConfig(
            mockInvitationService,
            mockLandlordService,
            mockPropertyOwnershipService,
            mockAbsoluteUrlProvider,
            mockAcceptedEmailSender,
            mockOtherLandlordEmailSender,
        )

    private fun setupTokenValidation(tokenIsValid: Boolean) {
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
        whenever(mockInvitationService.getTokenIsValid(token)).thenReturn(tokenIsValid)
    }

    private fun setupValidTokenWithInvitation(
        invitation: JointLandlordInvitation = MockJointLandlordData.createJointLandlordInvitation(),
    ): JointLandlordInvitation {
        setupTokenValidation(true)
        whenever(mockInvitationService.getInvitationForJourney(journeyId)).thenReturn(invitation)
        setMockPrincipal(baseUserId)
        return invitation
    }

    private fun setupValidTokenWithLandlord(
        invitation: JointLandlordInvitation = MockJointLandlordData.createJointLandlordInvitation(),
    ): Landlord {
        setupValidTokenWithInvitation(invitation)
        val landlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId))
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(landlord)
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(any())).thenReturn(URI("https://example.com/property"))
        return landlord
    }

    private fun setupValidTokenWithLandlordAndOwnership(
        acceptingLandlord: Landlord,
        propertyOwnership: PropertyOwnership,
    ) {
        val invitation = MockJointLandlordData.createJointLandlordInvitation(propertyOwnership = propertyOwnership)
        setupValidTokenWithInvitation(invitation)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(acceptingLandlord)
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(any())).thenReturn(URI("https://example.com/property"))
    }

    private fun setMockPrincipal(name: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
