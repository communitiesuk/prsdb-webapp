package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.mock.web.MockHttpSession
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_TOKEN
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
    private lateinit var mockHttpSession: MockHttpSession
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
    inner class GetPendingInvitationsTests {
        @Test
        fun `getPendingInvitations returns only non-expired invitations`() {
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

            val result = invitationService.getPendingInvitations(propertyOwnership)

            assertEquals(1, result.size)
            assertEquals(pendingInvitation, result[0])
        }

        @Test
        fun `getPendingInvitations returns results sorted by createdDate descending`() {
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

            val result = invitationService.getPendingInvitations(propertyOwnership)

            assertEquals(2, result.size)
            assertEquals(newerInvitation, result[0])
            assertEquals(olderInvitation, result[1])
        }
    }

    @Nested
    inner class GetExpiredInvitationsTests {
        @Test
        fun `getExpiredInvitations returns only expired invitations`() {
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

            val result = invitationService.getExpiredInvitations(propertyOwnership)

            assertEquals(1, result.size)
            assertEquals(expiredInvitation, result[0])
        }

        @Test
        fun `getExpiredInvitations returns results sorted by createdDate descending`() {
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

            val result = invitationService.getExpiredInvitations(propertyOwnership)

            assertEquals(2, result.size)
            assertEquals(newerExpired, result[0])
            assertEquals(olderExpired, result[1])
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
    inner class SessionTokenTests {
        @Test
        fun `storeTokenInSession stores the token under JOINT_LANDLORD_INVITATION_TOKEN`() {
            invitationService.storeTokenInSession("test-token-123")

            verify(mockHttpSession).setAttribute(JOINT_LANDLORD_INVITATION_TOKEN, "test-token-123")
        }

        @Test
        fun `getTokenFromSession retrieves the value under JOINT_LANDLORD_INVITATION_TOKEN`() {
            invitationService.getTokenFromSession()

            verify(mockHttpSession).getAttribute(JOINT_LANDLORD_INVITATION_TOKEN)
        }

        @Test
        fun `clearTokenFromSession clears JOINT_LANDLORD_INVITATION_TOKEN`() {
            invitationService.clearTokenFromSession()
            verify(mockHttpSession).removeAttribute(JOINT_LANDLORD_INVITATION_TOKEN)
        }
    }
}
