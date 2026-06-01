package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpSession
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

class JointLandlordInvitationServiceTests {
    private lateinit var mockJointLandlordInvitationRepository: JointLandlordInvitationRepository
    private lateinit var mockEmailNotificationService: EmailNotificationService<JointLandlordInvitationEmail>
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var mockHttpSession: MockHttpSession
    private lateinit var invitationService: JointLandlordInvitationService
    private lateinit var invitingLandlord: Landlord

    @BeforeEach
    fun setup() {
        mockJointLandlordInvitationRepository = mock()
        mockEmailNotificationService = mock()
        mockAbsoluteUrlProvider = mock()
        mockHttpSession = MockHttpSession()
        invitationService =
            JointLandlordInvitationService(
                mockJointLandlordInvitationRepository,
                mockEmailNotificationService,
                mockAbsoluteUrlProvider,
                mockHttpSession,
            )
        invitingLandlord = MockLandlordData.createLandlord()
    }

    @Test
    fun `sendInvitationEmails creates invitation tokens for each email address`() {
        val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com", "landlord3@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val mockToken = "test-token-123"
        val mockUri = URI("https://example.com/invite/$mockToken")

        whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
            .thenReturn(mockUri)

        invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

        verify(mockJointLandlordInvitationRepository, times(3)).save(any())
    }

    @Test
    fun `sendInvitationEmails sends an email to each joint landlord`() {
        val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val mockUri = URI("https://example.com/invite/test-token")

        whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
            .thenReturn(mockUri)

        invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

        val emailCaptor = argumentCaptor<String>()
        verify(mockEmailNotificationService, times(2))
            .sendEmail(emailCaptor.capture(), any())

        assertEquals(jointLandlordEmails, emailCaptor.allValues)
    }

    @Test
    fun `sendInvitationEmails includes correct sender name and property address in email`() {
        val jointLandlordEmails = listOf("landlord1@example.com")
        val landlord = MockLandlordData.createLandlord(name = "John Smith")
        val address = MockLandlordData.createAddress(singleLineAddress = "123 Test Street, London, SW1A 1AA")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                address = address,
            )
        val mockUri = URI("https://example.com/invite/test-token")

        whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
            .thenReturn(mockUri)

        invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, landlord)

        val emailModelCaptor = argumentCaptor<JointLandlordInvitationEmail>()
        verify(mockEmailNotificationService)
            .sendEmail(eq("landlord1@example.com"), emailModelCaptor.capture())

        assertEquals("John Smith", emailModelCaptor.firstValue.senderName)
        assertEquals("123 Test Street\nLondon\nSW1A 1AA", emailModelCaptor.firstValue.propertyAddress)
        assertEquals(mockUri, emailModelCaptor.firstValue.invitationUri)
    }

    @Test
    fun `sendInvitationEmails creates unique tokens for each invitation`() {
        val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val mockUri1 = URI("https://example.com/invite/token-1")
        val mockUri2 = URI("https://example.com/invite/token-2")

        whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
            .thenReturn(mockUri1, mockUri2)

        invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

        verify(mockJointLandlordInvitationRepository, times(2)).save(any())
        verify(mockAbsoluteUrlProvider, times(2)).buildJointLandlordInvitationUri(any())
    }

    @Test
    fun `sendInvitationEmails handles empty list without error`() {
        val jointLandlordEmails = emptyList<String>()
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

        verify(mockJointLandlordInvitationRepository, times(0)).save(any())
        verify(mockEmailNotificationService, times(0)).sendEmail(any(), any())
    }

    @Test
    fun `getJourneyIdInvitationTokenPairsFromSession returns null when no pairs are stored`() {
        // Act
        val result = invitationService.getJourneyIdInvitationTokenPairsFromSession()

        // Assert
        assertEquals(null, result)
    }

    @Test
    fun `getJourneyIdInvitationTokenPairsFromSession returns stored pairs`() {
        // Arrange
        val pairs = mutableListOf(Pair("journey-1", "token-1"))
        mockHttpSession.setAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS, pairs)

        // Act
        val result = invitationService.getJourneyIdInvitationTokenPairsFromSession()

        // Assert
        assertEquals(pairs, result)
    }

    @Test
    fun `addJourneyIdInvitationTokenPairToSession adds a pair when session is empty`() {
        // Act
        invitationService.addJourneyIdInvitationTokenPairToSession("journey-1", "token-1")

        // Assert
        val stored = invitationService.getJourneyIdInvitationTokenPairsFromSession()
        assertEquals(1, stored?.size)
        assertEquals(Pair("journey-1", "token-1"), stored?.first())
    }

    @Test
    fun `addJourneyIdInvitationTokenPairToSession appends to existing pairs`() {
        // Arrange
        invitationService.addJourneyIdInvitationTokenPairToSession("journey-1", "token-1")

        // Act
        invitationService.addJourneyIdInvitationTokenPairToSession("journey-2", "token-2")

        // Assert
        val stored = invitationService.getJourneyIdInvitationTokenPairsFromSession()
        assertEquals(2, stored?.size)
        assertEquals(Pair("journey-1", "token-1"), stored?.get(0))
        assertEquals(Pair("journey-2", "token-2"), stored?.get(1))
    }

    @Test
    fun `getInvitationTokenForJourneyIdFromSession returns null when no pairs are stored`() {
        // Act
        val result = invitationService.getInvitationTokenForJourneyIdFromSession("journey-1")

        // Assert
        assertEquals(null, result)
    }

    @Test
    fun `getInvitationTokenForJourneyIdFromSession returns null when journey id is not found`() {
        // Arrange
        invitationService.addJourneyIdInvitationTokenPairToSession("journey-1", "token-1")

        // Act
        val result = invitationService.getInvitationTokenForJourneyIdFromSession("journey-2")

        // Assert
        assertEquals(null, result)
    }

    @Test
    fun `getInvitationTokenForJourneyIdFromSession returns matching token for journey id`() {
        // Arrange
        invitationService.addJourneyIdInvitationTokenPairToSession("journey-1", "token-1")
        invitationService.addJourneyIdInvitationTokenPairToSession("journey-2", "token-2")

        // Act
        val result = invitationService.getInvitationTokenForJourneyIdFromSession("journey-2")

        // Assert
        assertEquals("token-2", result)
    }

    @Test
    fun `clearJourneyIdInvitationTokenPairsForTokenFromSession removes all pairs with matching token`() {
        // Arrange
        invitationService.addJourneyIdInvitationTokenPairToSession("journey-1", "token-1")
        invitationService.addJourneyIdInvitationTokenPairToSession("journey-2", "token-1")
        invitationService.addJourneyIdInvitationTokenPairToSession("journey-3", "token-2")

        // Act
        invitationService.clearJourneyIdInvitationTokenPairsForTokenFromSession("token-1")

        // Assert
        val stored = invitationService.getJourneyIdInvitationTokenPairsFromSession()
        assertEquals(1, stored?.size)
        assertEquals(Pair("journey-3", "token-2"), stored?.first())
    }

    @Test
    fun `clearJourneyIdInvitationTokenPairsForTokenFromSession leaves other pairs intact when token not found`() {
        // Arrange
        invitationService.addJourneyIdInvitationTokenPairToSession("journey-1", "token-1")

        // Act
        invitationService.clearJourneyIdInvitationTokenPairsForTokenFromSession("non-existent-token")

        // Assert
        val stored = invitationService.getJourneyIdInvitationTokenPairsFromSession()
        assertEquals(1, stored?.size)
        assertEquals(Pair("journey-1", "token-1"), stored?.first())
    }

    @Test
    fun `addLandlordRegistrationAndAcceptanceJourneyIdPairsToSession stores the pair`() {
        // Act
        invitationService.addLandlordRegistrationAndAcceptanceJourneyIdPairsToSession("reg-journey-1", "acceptance-journey-1")

        // Assert
        val result = invitationService.getLandlordRegistrationJourneyIdForAcceptanceJourneyIdFromSession("acceptance-journey-1")
        assertEquals("reg-journey-1", result)
    }

    @Test
    fun `getLandlordRegistrationJourneyIdForAcceptanceJourneyIdFromSession returns null when no pairs stored`() {
        // Act
        val result = invitationService.getLandlordRegistrationJourneyIdForAcceptanceJourneyIdFromSession("acceptance-journey-1")

        // Assert
        assertEquals(null, result)
    }

    @Test
    fun `getLandlordRegistrationJourneyIdForAcceptanceJourneyIdFromSession returns null when acceptance journey id not found`() {
        // Arrange
        invitationService.addLandlordRegistrationAndAcceptanceJourneyIdPairsToSession("reg-journey-1", "acceptance-journey-1")

        // Act
        val result = invitationService.getLandlordRegistrationJourneyIdForAcceptanceJourneyIdFromSession("acceptance-journey-2")

        // Assert
        assertEquals(null, result)
    }

    @Test
    fun `getLandlordRegistrationJourneyIdForAcceptanceJourneyIdFromSession returns correct registration journey id`() {
        // Arrange
        invitationService.addLandlordRegistrationAndAcceptanceJourneyIdPairsToSession("reg-journey-1", "acceptance-journey-1")
        invitationService.addLandlordRegistrationAndAcceptanceJourneyIdPairsToSession("reg-journey-2", "acceptance-journey-2")

        // Act
        val result = invitationService.getLandlordRegistrationJourneyIdForAcceptanceJourneyIdFromSession("acceptance-journey-2")

        // Assert
        assertEquals("reg-journey-2", result)
    }

    @Test
    fun `addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession stores the value`() {
        // Act
        invitationService.addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession("journey-1", true)

        // Assert
        val result = invitationService.getUserSentToLandlordRegistrationWhileAcceptingThisJointLandlordInvitationFromSession("journey-1")
        assertEquals(true, result)
    }

    @Test
    fun `addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession updates existing entry for same journey id`() {
        // Arrange
        invitationService.addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession("journey-1", true)

        // Act
        invitationService.addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession("journey-1", false)

        // Assert
        val result = invitationService.getUserSentToLandlordRegistrationWhileAcceptingThisJointLandlordInvitationFromSession("journey-1")
        assertEquals(false, result)
    }

    @Test
    fun `addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession adds separate entries for different journey ids`() {
        // Act
        invitationService.addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession("journey-1", true)
        invitationService.addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession("journey-2", false)

        // Assert
        assertEquals(
            true,
            invitationService.getUserSentToLandlordRegistrationWhileAcceptingThisJointLandlordInvitationFromSession("journey-1"),
        )
        assertEquals(
            false,
            invitationService.getUserSentToLandlordRegistrationWhileAcceptingThisJointLandlordInvitationFromSession("journey-2"),
        )
    }

    @Test
    fun `getJointLandlordInvitationJourneyIdForLandlordRegistrationFromSession returns null when no pairs stored`() {
        // Act
        val result = invitationService.getJointLandlordInvitationJourneyIdForLandlordRegistrationFromSession()

        // Assert
        assertEquals(null, result)
    }

    @Test
    fun `getJointLandlordInvitationJourneyIdForLandlordRegistrationFromSession returns journey id where user was sent to registration`() {
        // Arrange
        invitationService.addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession("journey-1", false)
        invitationService.addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession("journey-2", true)

        // Act
        val result = invitationService.getJointLandlordInvitationJourneyIdForLandlordRegistrationFromSession()

        // Assert
        assertEquals("journey-2", result)
    }

    @Test
    fun `getJointLandlordInvitationJourneyIdForLandlordRegistrationFromSession returns null when no user was sent to registration`() {
        // Arrange
        invitationService.addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession("journey-1", false)

        // Act
        val result = invitationService.getJointLandlordInvitationJourneyIdForLandlordRegistrationFromSession()

        // Assert
        assertEquals(null, result)
    }

    @Test
    fun `getUserSentToLandlordRegistrationWhileAcceptingThisJointLandlordInvitationFromSession returns null when no pairs stored`() {
        // Act
        val result = invitationService.getUserSentToLandlordRegistrationWhileAcceptingThisJointLandlordInvitationFromSession("journey-1")

        // Assert
        assertEquals(null, result)
    }

    @Test
    fun `getUserSentToLandlordRegistrationWhileAcceptingThisJointLandlordInvitationFromSession returns null when journey id not found`() {
        // Arrange
        invitationService.addUserSentToLandlordRegistrationFromJointLandlordInvitationToSession("journey-1", true)

        // Act
        val result = invitationService.getUserSentToLandlordRegistrationWhileAcceptingThisJointLandlordInvitationFromSession("journey-2")

        // Assert
        assertEquals(null, result)
    }
}
