package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit

class JointLandlordInvitationServiceTests {
    private lateinit var mockJointLandlordInvitationRepository: JointLandlordInvitationRepository
    private lateinit var mockEmailNotificationService: EmailNotificationService<JointLandlordInvitationEmail>
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var mockHttpSession: HttpSession
    private lateinit var invitationService: JointLandlordInvitationService
    private lateinit var invitingLandlord: Landlord

    @BeforeEach
    fun setup() {
        mockJointLandlordInvitationRepository = mock()
        mockEmailNotificationService = mock()
        mockAbsoluteUrlProvider = mock()
        mockHttpSession = mock()
        invitationService =
            JointLandlordInvitationService(
                mockJointLandlordInvitationRepository,
                mockEmailNotificationService,
                mockAbsoluteUrlProvider,
                mockHttpSession,
            )
        invitingLandlord = MockLandlordData.createLandlord()
    }

    @Nested
    inner class GetPendingAndExpiredInvitationsTests {
        @Test
        fun `getPendingAndExpiredInvitations partitions into pending and expired`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val pendingInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now(),
                )
            val expiredInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    id = 456L,
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(), ChronoUnit.DAYS),
                )

            whenever(mockJointLandlordInvitationRepository.findByRegisteredOwnership(propertyOwnership))
                .thenReturn(listOf(pendingInvitation, expiredInvitation))

            val (pending, expired) = invitationService.getPendingAndExpiredInvitations(propertyOwnership)

            assertEquals(1, pending.size)
            assertEquals(pendingInvitation, pending[0])
            assertEquals(1, expired.size)
            assertEquals(expiredInvitation, expired[0])
        }

        @Test
        fun `getPendingAndExpiredInvitations returns pending results sorted by createdDate descending`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val olderInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    id = 1L,
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now().minus(10, ChronoUnit.DAYS),
                )
            val newerInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    id = 2L,
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now().minus(1, ChronoUnit.DAYS),
                )

            whenever(mockJointLandlordInvitationRepository.findByRegisteredOwnership(propertyOwnership))
                .thenReturn(listOf(olderInvitation, newerInvitation))

            val (pending, _) = invitationService.getPendingAndExpiredInvitations(propertyOwnership)

            assertEquals(2, pending.size)
            assertEquals(newerInvitation, pending[0])
            assertEquals(olderInvitation, pending[1])
        }

        @Test
        fun `getPendingAndExpiredInvitations returns expired results sorted by createdDate descending`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val olderExpired =
                MockJointLandlordData.createJointLandlordInvitation(
                    id = 1L,
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 32).toLong(), ChronoUnit.DAYS),
                )
            val newerExpired =
                MockJointLandlordData.createJointLandlordInvitation(
                    id = 2L,
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 2).toLong(), ChronoUnit.DAYS),
                )

            whenever(mockJointLandlordInvitationRepository.findByRegisteredOwnership(propertyOwnership))
                .thenReturn(listOf(olderExpired, newerExpired))

            val (_, expired) = invitationService.getPendingAndExpiredInvitations(propertyOwnership)

            assertEquals(2, expired.size)
            assertEquals(newerExpired, expired[0])
            assertEquals(olderExpired, expired[1])
        }

        @Test
        fun `getPendingAndExpiredInvitations makes a single repository call`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()

            whenever(mockJointLandlordInvitationRepository.findByRegisteredOwnership(propertyOwnership))
                .thenReturn(emptyList())

            invitationService.getPendingAndExpiredInvitations(propertyOwnership)

            verify(mockJointLandlordInvitationRepository, times(1)).findByRegisteredOwnership(propertyOwnership)
        }
    }

    @Nested
    inner class SendInvitationEmailsTests {
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
    }

    @Nested
    inner class GetJourneyIdInvitationTokenPairsFromSession {
        @Test
        fun `getJourneyIdInvitationTokenPairsFromSession returns null when session has no attribute`() {
            // Arrange
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(null)

            // Act & Assert
            assertNull(invitationService.getJourneyIdInvitationTokenPairsFromSession())
        }

        @Test
        fun `getJourneyIdInvitationTokenPairsFromSession returns pairs when session has attribute`() {
            // Arrange
            val pairs = mutableListOf(Pair("journey1", "token1"), Pair("journey2", "token2"))
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(pairs)

            // Act & Assert
            assertEquals(pairs, invitationService.getJourneyIdInvitationTokenPairsFromSession())
        }
    }

    @Nested
    inner class AddJourneyIdInvitationTokenPairToSession {
        @Test
        fun `addJourneyIdInvitationTokenPairToSession adds pair to empty session`() {
            // Arrange
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(null)

            // Act
            invitationService.addJourneyIdInvitationTokenPairToSession("journey1", "token1")

            // Assert
            val captor = argumentCaptor<MutableList<Pair<String, String>>>()
            verify(mockHttpSession).setAttribute(
                eq(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS),
                captor.capture(),
            )
            assertEquals(listOf(Pair("journey1", "token1")), captor.firstValue)
        }

        @Test
        fun `addJourneyIdInvitationTokenPairToSession appends pair to existing pairs`() {
            // Arrange
            val existingPairs = mutableListOf(Pair("journey1", "token1"))
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(existingPairs)

            // Act
            invitationService.addJourneyIdInvitationTokenPairToSession("journey2", "token2")

            // Assert
            val captor = argumentCaptor<MutableList<Pair<String, String>>>()
            verify(mockHttpSession).setAttribute(
                eq(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS),
                captor.capture(),
            )
            assertEquals(listOf(Pair("journey1", "token1"), Pair("journey2", "token2")), captor.firstValue)
        }
    }

    @Nested
    inner class GetInvitationTokenForJourneyIdFromSession {
        @Test
        fun `getInvitationTokenForJourneyIdFromSession returns token when journey id exists`() {
            // Arrange
            val pairs = mutableListOf(Pair("journey1", "token1"), Pair("journey2", "token2"))
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(pairs)

            // Act & Assert
            assertEquals("token2", invitationService.getInvitationTokenForJourneyIdFromSession("journey2"))
        }

        @Test
        fun `getInvitationTokenForJourneyIdFromSession returns null when journey id does not exist`() {
            // Arrange
            val pairs = mutableListOf(Pair("journey1", "token1"))
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(pairs)

            // Act & Assert
            assertNull(invitationService.getInvitationTokenForJourneyIdFromSession("nonexistent"))
        }

        @Test
        fun `getInvitationTokenForJourneyIdFromSession returns null when session has no pairs`() {
            // Arrange
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(null)

            // Act & Assert
            assertNull(invitationService.getInvitationTokenForJourneyIdFromSession("journey1"))
        }
    }

    @Nested
    inner class ClearJourneyIdInvitationTokenPairsForTokenFromSession {
        @Test
        fun `clearJourneyIdInvitationTokenPairsForTokenFromSession removes all pairs with matching token`() {
            // Arrange
            val pairs = mutableListOf(Pair("journey1", "token1"), Pair("journey2", "token1"), Pair("journey3", "token2"))
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(pairs)

            // Act
            invitationService.clearJourneyIdInvitationTokenPairsForTokenFromSession("token1")

            // Assert
            val captor = argumentCaptor<List<Pair<String, String>>>()
            verify(mockHttpSession).setAttribute(
                eq(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS),
                captor.capture(),
            )
            assertEquals(listOf(Pair("journey3", "token2")), captor.firstValue)
        }

        @Test
        fun `clearJourneyIdInvitationTokenPairsForTokenFromSession sets empty list when all pairs match`() {
            // Arrange
            val pairs = mutableListOf(Pair("journey1", "token1"), Pair("journey2", "token1"))
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(pairs)

            // Act
            invitationService.clearJourneyIdInvitationTokenPairsForTokenFromSession("token1")

            // Assert
            val captor = argumentCaptor<List<Pair<String, String>>>()
            verify(mockHttpSession).setAttribute(
                eq(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS),
                captor.capture(),
            )
            assertEquals(emptyList<Pair<String, String>>(), captor.firstValue)
        }
    }

    @Nested
    inner class AddOrUpdateUserSentToLandlordRegistrationTaskToSession {
        @Test
        fun `addOrUpdateUserSentToLandlordRegistrationTaskToSession adds new entry when session is empty`() {
            // Arrange
            whenever(mockHttpSession.getAttribute(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION))
                .thenReturn(null)

            // Act
            invitationService.addOrUpdateUserSentToLandlordRegistrationTaskToSession("journey1", true)

            // Assert
            val captor = argumentCaptor<MutableList<Pair<String, Boolean>>>()
            verify(mockHttpSession).setAttribute(
                eq(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION),
                captor.capture(),
            )
            assertEquals(listOf(Pair("journey1", true)), captor.firstValue)
        }

        @Test
        fun `addOrUpdateUserSentToLandlordRegistrationTaskToSession updates existing entry for same journey id`() {
            // Arrange
            val existingPairs = mutableListOf(Pair("journey1", false))
            whenever(mockHttpSession.getAttribute(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION))
                .thenReturn(existingPairs)

            // Act
            invitationService.addOrUpdateUserSentToLandlordRegistrationTaskToSession("journey1", true)

            // Assert
            val captor = argumentCaptor<MutableList<Pair<String, Boolean>>>()
            verify(mockHttpSession).setAttribute(
                eq(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION),
                captor.capture(),
            )
            assertEquals(listOf(Pair("journey1", true)), captor.firstValue)
        }

        @Test
        fun `addOrUpdateUserSentToLandlordRegistrationTaskToSession adds new entry alongside existing entries`() {
            // Arrange
            val existingPairs = mutableListOf(Pair("journey1", true))
            whenever(mockHttpSession.getAttribute(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION))
                .thenReturn(existingPairs)

            // Act
            invitationService.addOrUpdateUserSentToLandlordRegistrationTaskToSession("journey2", false)

            // Assert
            val captor = argumentCaptor<MutableList<Pair<String, Boolean>>>()
            verify(mockHttpSession).setAttribute(
                eq(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION),
                captor.capture(),
            )
            assertEquals(listOf(Pair("journey1", true), Pair("journey2", false)), captor.firstValue)
        }
    }

    @Nested
    inner class GetUserSentToLandlordRegistrationTaskFromSession {
        @Test
        fun `getUserSentToLandlordRegistrationTaskFromSession returns value when journey id exists`() {
            // Arrange
            val pairs = mutableListOf(Pair("journey1", true), Pair("journey2", false))
            whenever(mockHttpSession.getAttribute(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION))
                .thenReturn(pairs)

            // Act & Assert
            assertEquals(true, invitationService.getUserSentToLandlordRegistrationTaskFromSession("journey1"))
        }

        @Test
        fun `getUserSentToLandlordRegistrationTaskFromSession returns null when journey id does not exist`() {
            // Arrange
            val pairs = mutableListOf(Pair("journey1", true))
            whenever(mockHttpSession.getAttribute(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION))
                .thenReturn(pairs)

            // Act & Assert
            assertNull(invitationService.getUserSentToLandlordRegistrationTaskFromSession("nonexistent"))
        }

        @Test
        fun `getUserSentToLandlordRegistrationTaskFromSession returns null when session has no attribute`() {
            // Arrange
            whenever(mockHttpSession.getAttribute(USER_SENT_TO_LANDLORD_REGISTRATION_WHILE_ACCEPTING_JOINT_LANDLORD_INVITATION))
                .thenReturn(null)

            // Act & Assert
            assertNull(invitationService.getUserSentToLandlordRegistrationTaskFromSession("journey1"))
        }
    }
}
