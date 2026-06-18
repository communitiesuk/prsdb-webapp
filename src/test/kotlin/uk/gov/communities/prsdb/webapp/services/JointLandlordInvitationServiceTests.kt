package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.ACCEPTED_JOINT_LANDLORD_PROPERTY_DETAILS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_EMAIL_CANCELLED
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.enums.JointLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationNotifyExistingEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID

class JointLandlordInvitationServiceTests {
    private lateinit var mockJointLandlordInvitationRepository: JointLandlordInvitationRepository
    private lateinit var mockInvitationEmailSender: EmailNotificationService<JointLandlordInvitationEmail>
    private lateinit var mockConfirmationEmailSender: EmailNotificationService<JointLandlordInvitationConfirmationEmail>
    private lateinit var mockNotifyExistingEmailSender: EmailNotificationService<JointLandlordInvitationNotifyExistingEmail>
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var mockHttpSession: HttpSession
    private lateinit var invitationService: JointLandlordInvitationService
    private lateinit var invitingLandlord: Landlord

    @BeforeEach
    fun setup() {
        mockJointLandlordInvitationRepository = mock()
        mockInvitationEmailSender = mock()
        mockConfirmationEmailSender = mock()
        mockNotifyExistingEmailSender = mock()
        mockAbsoluteUrlProvider = mock()
        mockHttpSession = mock()
        invitationService =
            JointLandlordInvitationService(
                mockJointLandlordInvitationRepository,
                mockInvitationEmailSender,
                mockConfirmationEmailSender,
                mockNotifyExistingEmailSender,
                mockAbsoluteUrlProvider,
                mockHttpSession,
            )
        invitingLandlord = MockLandlordData.createLandlord()

        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(any()))
            .thenReturn(URI("https://example.com/property/1"))
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
        fun `getPendingAndExpiredInvitations excludes hidden invitations`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val pendingInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now(),
                )
            val hiddenInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    id = 456L,
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now(),
                    isHidden = true,
                )

            whenever(mockJointLandlordInvitationRepository.findByRegisteredOwnership(propertyOwnership))
                .thenReturn(listOf(pendingInvitation, hiddenInvitation))

            val (pending, expired) = invitationService.getPendingAndExpiredInvitations(propertyOwnership)

            assertEquals(1, pending.size)
            assertEquals(pendingInvitation, pending[0])
            assertEquals(0, expired.size)
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
        fun `getPendingAndExpiredInvitations excludes hidden expired invitations`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val expiredInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    id = 1L,
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(), ChronoUnit.DAYS),
                )
            val hiddenExpiredInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    id = 2L,
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(), ChronoUnit.DAYS),
                    isHidden = true,
                )

            whenever(mockJointLandlordInvitationRepository.findByRegisteredOwnership(propertyOwnership))
                .thenReturn(listOf(expiredInvitation, hiddenExpiredInvitation))

            val (pending, expired) = invitationService.getPendingAndExpiredInvitations(propertyOwnership)

            assertEquals(0, pending.size)
            assertEquals(1, expired.size)
            assertEquals(expiredInvitation, expired[0])
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
            verify(mockInvitationEmailSender, times(2))
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
            verify(mockInvitationEmailSender)
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
            verify(mockInvitationEmailSender, times(0)).sendEmail(any(), any())
        }

        @Test
        fun `sendInvitationEmails sends confirmation email to inviting landlord`() {
            val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com")
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val mockUri = URI("https://example.com/invite/test-token")

            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
                .thenReturn(mockUri)

            invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

            verify(mockConfirmationEmailSender).sendEmail(eq(invitingLandlord.email), any())
        }

        @Test
        fun `sendInvitationEmails does not send confirmation email when list is empty`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()

            invitationService.sendInvitationEmails(emptyList(), propertyOwnership, invitingLandlord)

            verify(mockConfirmationEmailSender, times(0)).sendEmail(any(), any())
        }

        @Test
        fun `sendInvitationEmails includes correct property details url in confirmation email`() {
            val jointLandlordEmails = listOf("landlord1@example.com")
            val propertyDetailsUri = URI("https://example.com/property/123")
            val mockUri = URI("https://example.com/invite/test-token")
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 123L)

            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
                .thenReturn(mockUri)
            whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(123L)).thenReturn(propertyDetailsUri)

            invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

            val emailModelCaptor = argumentCaptor<JointLandlordInvitationConfirmationEmail>()
            verify(mockConfirmationEmailSender).sendEmail(eq(invitingLandlord.email), emailModelCaptor.capture())

            assertEquals(propertyDetailsUri.toString(), emailModelCaptor.firstValue.propertyRecordUrl)
        }

        @Test
        fun `sendInvitationEmails formats single email without bullets in confirmation`() {
            val jointLandlordEmails = listOf("landlord1@example.com")
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 123L)
            val mockUri = URI("https://example.com/invite/test-token")
            val propertyDetailsUri = URI("https://example.com/property/123")

            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
                .thenReturn(mockUri)
            whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(123L)).thenReturn(propertyDetailsUri)

            invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

            val emailModelCaptor = argumentCaptor<JointLandlordInvitationConfirmationEmail>()
            verify(mockConfirmationEmailSender).sendEmail(eq(invitingLandlord.email), emailModelCaptor.capture())

            val emailMap = emailModelCaptor.firstValue.toHashMap()
            assertEquals("landlord1@example.com", emailMap["landlord invites"])
        }

        @Test
        fun `sendInvitationEmails formats multiple emails as bullet list in confirmation`() {
            val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com")
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 123L)
            val mockUri = URI("https://example.com/invite/test-token")
            val propertyDetailsUri = URI("https://example.com/property/123")

            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
                .thenReturn(mockUri)
            whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(123L)).thenReturn(propertyDetailsUri)

            invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

            val emailModelCaptor = argumentCaptor<JointLandlordInvitationConfirmationEmail>()
            verify(mockConfirmationEmailSender).sendEmail(eq(invitingLandlord.email), emailModelCaptor.capture())

            val emailMap = emailModelCaptor.firstValue.toHashMap()
            assertEquals("* landlord1@example.com\n* landlord2@example.com", emailMap["landlord invites"])
        }

        @Test
        fun `sendInvitationEmails sends notify-existing email to other landlords on property`() {
            val jointLandlordEmails = listOf("new@example.com")
            val existingLandlord = MockLandlordData.createLandlord(name = "Existing", email = "existing@example.com")
            ReflectionTestUtils.setField(existingLandlord, "id", 2L)
            ReflectionTestUtils.setField(invitingLandlord, "id", 1L)
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 123L, primaryLandlord = invitingLandlord)
            propertyOwnership.addLandlord(existingLandlord)
            val mockUri = URI("https://example.com/invite/test-token")

            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any())).thenReturn(mockUri)

            invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

            val emailModelCaptor = argumentCaptor<JointLandlordInvitationNotifyExistingEmail>()
            verify(mockNotifyExistingEmailSender).sendEmail(eq("existing@example.com"), emailModelCaptor.capture())
            assertEquals("Existing", emailModelCaptor.firstValue.recipientName)
            assertEquals(listOf("new@example.com"), emailModelCaptor.firstValue.jointLandlordEmails)
        }

        @Test
        fun `sendInvitationEmails does not send notify-existing email to inviting landlord`() {
            val jointLandlordEmails = listOf("new@example.com")
            ReflectionTestUtils.setField(invitingLandlord, "id", 1L)
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 123L, primaryLandlord = invitingLandlord)
            val mockUri = URI("https://example.com/invite/test-token")

            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any())).thenReturn(mockUri)

            invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

            verify(mockNotifyExistingEmailSender, times(0)).sendEmail(any(), any())
        }

        @Test
        fun `sendInvitationEmails does not save the invitation when sending the email fails`() {
            val jointLandlordEmails = listOf("landlord1@example.com")
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val mockUri = URI("https://example.com/invite/test-token")

            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any())).thenReturn(mockUri)
            whenever(mockInvitationEmailSender.sendEmail(any(), any()))
                .thenThrow(RuntimeException("Email failed to send"))

            assertThrows(RuntimeException::class.java) {
                invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)
            }

            verify(mockJointLandlordInvitationRepository, times(0)).save(any())
        }

        @Test
        fun `sendInvitationEmails only saves invitations for emails sent before a failure`() {
            val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com")
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val mockUri = URI("https://example.com/invite/test-token")

            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any())).thenReturn(mockUri)
            whenever(mockInvitationEmailSender.sendEmail(any(), any()))
                .thenAnswer { /* succeed on the first call */ }
                .thenThrow(RuntimeException("Email failed to send"))

            assertThrows(RuntimeException::class.java) {
                invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)
            }

            verify(mockJointLandlordInvitationRepository, times(1)).save(any())
            verify(mockConfirmationEmailSender, times(0)).sendEmail(any(), any())
        }

        @Test
        fun `sendInvitationEmails skips emails already invited on the property`() {
            val jointLandlordEmails = listOf("already.invited@example.com", "new@example.com")
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 123L)
            val mockUri = URI("https://example.com/invite/test-token")

            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any())).thenReturn(mockUri)
            whenever(mockJointLandlordInvitationRepository.findByRegisteredOwnershipId(123L))
                .thenReturn(listOf(MockJointLandlordData.createJointLandlordInvitation(email = "already.invited@example.com")))

            invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

            val emailCaptor = argumentCaptor<String>()
            verify(mockInvitationEmailSender, times(1)).sendEmail(emailCaptor.capture(), any())
            assertEquals(listOf("new@example.com"), emailCaptor.allValues)
            verify(mockJointLandlordInvitationRepository, times(1)).save(any())
        }

        @Test
        fun `sendInvitationEmails skips emails already a landlord on the property`() {
            val existingLandlord = MockLandlordData.createLandlord(email = "existing@example.com")
            val propertyOwnership =
                MockLandlordData.createPropertyOwnership(
                    id = 123L,
                    otherLandlords = mutableSetOf(existingLandlord),
                )
            val jointLandlordEmails = listOf("existing@example.com", "new@example.com")
            val mockUri = URI("https://example.com/invite/test-token")

            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any())).thenReturn(mockUri)

            invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

            val emailCaptor = argumentCaptor<String>()
            verify(mockInvitationEmailSender, times(1)).sendEmail(emailCaptor.capture(), any())
            assertEquals(listOf("new@example.com"), emailCaptor.allValues)
            verify(mockJointLandlordInvitationRepository, times(1)).save(any())
        }

        @Test
        fun `sendInvitationEmails does not send any emails when all addresses are already invited`() {
            val jointLandlordEmails = listOf("already.invited@example.com")
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 123L)

            whenever(mockJointLandlordInvitationRepository.findByRegisteredOwnershipId(123L))
                .thenReturn(listOf(MockJointLandlordData.createJointLandlordInvitation(email = "already.invited@example.com")))

            invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership, invitingLandlord)

            verify(mockJointLandlordInvitationRepository, times(0)).save(any())
            verify(mockInvitationEmailSender, times(0)).sendEmail(any(), any())
            verify(mockConfirmationEmailSender, times(0)).sendEmail(any(), any())
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
        fun `getInvitationTokenForJourneyIdFromSession throws when journey id does not exist`() {
            // Arrange
            val pairs = mutableListOf(Pair("journey1", "token1"))
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(pairs)

            // Act & Assert
            assertThrows<PrsdbWebException> {
                invitationService.getInvitationTokenForJourneyIdFromSession("nonexistent")
            }
        }

        @Test
        fun `getInvitationTokenForJourneyIdFromSession throws when session has no pairs`() {
            // Arrange
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(null)

            // Act & Assert
            assertThrows<PrsdbWebException> {
                invitationService.getInvitationTokenForJourneyIdFromSession("journey1")
            }
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
    inner class GetTokenIsValid {
        private val validToken = UUID.randomUUID().toString()

        @Test
        fun `getTokenIsValid returns true when token is a valid unexpired invitation`() {
            val mockInvitation = mock<JointLandlordInvitation>()
            whenever(mockJointLandlordInvitationRepository.findByToken(UUID.fromString(validToken))).thenReturn(mockInvitation)
            whenever(mockInvitation.status).thenReturn(JointLandlordInvitationStatus.PENDING)

            assertTrue(invitationService.getTokenIsValid(validToken))
        }

        @Test
        fun `getTokenIsValid returns false when token is not a valid UUID`() {
            assertFalse(invitationService.getTokenIsValid("not-a-valid-uuid"))
        }

        @Test
        fun `getTokenIsValid returns false when token is not found in the repository`() {
            whenever(mockJointLandlordInvitationRepository.findByToken(UUID.fromString(validToken))).thenReturn(null)

            assertFalse(invitationService.getTokenIsValid(validToken))
        }

        @Test
        fun `getTokenIsValid returns false when invitation has expired`() {
            val mockInvitation = mock<JointLandlordInvitation>()
            whenever(mockJointLandlordInvitationRepository.findByToken(UUID.fromString(validToken))).thenReturn(mockInvitation)
            whenever(mockInvitation.status).thenReturn(JointLandlordInvitationStatus.EXPIRED)

            assertFalse(invitationService.getTokenIsValid(validToken))
        }

        @Test
        fun `getTokenIsValid returns false when invitation has been hidden`() {
            val mockInvitation = mock<JointLandlordInvitation>()
            whenever(mockJointLandlordInvitationRepository.findByToken(UUID.fromString(validToken))).thenReturn(mockInvitation)
            whenever(mockInvitation.status).thenReturn(JointLandlordInvitationStatus.HIDDEN)

            assertFalse(invitationService.getTokenIsValid(validToken))
        }
    }

    @Nested
    inner class HideExpiredInvitationTests {
        private val baseUserId = "test-user-id"

        @Test
        fun `hideExpiredInvitation sets isHidden to true and saves the invitation`() {
            val baseUser = MockLandlordData.createPrsdbUser(baseUserId)
            val landlord = MockLandlordData.createLandlord(baseUser = baseUser)
            val propertyOwnership = MockLandlordData.createPropertyOwnership(primaryLandlord = landlord)
            val invitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    id = 1L,
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(), ChronoUnit.DAYS),
                )

            whenever(mockJointLandlordInvitationRepository.findById(1L))
                .thenReturn(Optional.of(invitation))

            invitationService.hideExpiredInvitation(1L, baseUserId)

            assertTrue(invitation.isHidden)
            verify(mockJointLandlordInvitationRepository).save(invitation)
        }

        @Test
        fun `hideExpiredInvitation throws NOT_FOUND when invitation does not exist`() {
            whenever(mockJointLandlordInvitationRepository.findById(999L))
                .thenReturn(Optional.empty())

            val exception =
                assertThrows(ResponseStatusException::class.java) {
                    invitationService.hideExpiredInvitation(999L, baseUserId)
                }

            assertEquals(404, exception.statusCode.value())
        }

        @Test
        fun `hideExpiredInvitation throws FORBIDDEN when user does not own the property`() {
            val otherUser = MockLandlordData.createPrsdbUser("other-user-id")
            val otherLandlord = MockLandlordData.createLandlord(baseUser = otherUser)
            val propertyOwnership = MockLandlordData.createPropertyOwnership(primaryLandlord = otherLandlord)
            val invitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    id = 1L,
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(), ChronoUnit.DAYS),
                )

            whenever(mockJointLandlordInvitationRepository.findById(1L))
                .thenReturn(Optional.of(invitation))

            val exception =
                assertThrows(ResponseStatusException::class.java) {
                    invitationService.hideExpiredInvitation(1L, baseUserId)
                }

            assertEquals(403, exception.statusCode.value())
        }

        @Test
        fun `hideExpiredInvitation throws BAD_REQUEST when invitation is not expired`() {
            val baseUser = MockLandlordData.createPrsdbUser(baseUserId)
            val landlord = MockLandlordData.createLandlord(baseUser = baseUser)
            val propertyOwnership = MockLandlordData.createPropertyOwnership(primaryLandlord = landlord)
            val invitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    id = 1L,
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now(),
                )

            whenever(mockJointLandlordInvitationRepository.findById(1L))
                .thenReturn(Optional.of(invitation))

            val exception =
                assertThrows(ResponseStatusException::class.java) {
                    invitationService.hideExpiredInvitation(1L, baseUserId)
                }

            assertEquals(400, exception.statusCode.value())
        }
    }

    @Nested
    inner class GetExistingInvitedEmailsTests {
        @Test
        fun `getExistingInvitedEmails returns emails from pending and expired invitations`() {
            val pendingInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    email = "pending@example.com",
                    createdDate = Instant.now(),
                )
            val expiredInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    email = "expired@example.com",
                    createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(), ChronoUnit.DAYS),
                )
            val hiddenInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    email = "hidden@example.com",
                    createdDate = Instant.now(),
                    isHidden = true,
                )

            whenever(mockJointLandlordInvitationRepository.findByRegisteredOwnershipId(1L))
                .thenReturn(listOf(pendingInvitation, expiredInvitation, hiddenInvitation))

            val result = invitationService.getExistingInvitedEmails(1L)

            assertEquals(listOf("pending@example.com", "expired@example.com"), result)
        }

        @Test
        fun `getExistingInvitedEmails returns empty list when no invitations exist`() {
            whenever(mockJointLandlordInvitationRepository.findByRegisteredOwnershipId(1L))
                .thenReturn(emptyList())

            val result = invitationService.getExistingInvitedEmails(1L)

            assertEquals(emptyList<String>(), result)
        }
    }

    @Nested
    inner class ResendInvitation {
        @Test
        fun `resendInvitation deletes old invitation flushes and creates a new one with the same token`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 1L)
            val oldInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    propertyOwnership = propertyOwnership,
                    invitingLandlordName = invitingLandlord.name,
                )
            val mockUri = URI("https://example.com/invite/new-token")

            whenever(mockJointLandlordInvitationRepository.findById(oldInvitation.id))
                .thenReturn(Optional.of(oldInvitation))
            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
                .thenReturn(mockUri)

            invitationService.resendInvitation(oldInvitation.id, propertyOwnership, invitingLandlord)

            val inOrder = inOrder(mockJointLandlordInvitationRepository)
            inOrder.verify(mockJointLandlordInvitationRepository).delete(oldInvitation)
            inOrder.verify(mockJointLandlordInvitationRepository).flush()
            inOrder.verify(mockJointLandlordInvitationRepository).save(
                argThat { token == oldInvitation.token },
            )
        }

        @Test
        fun `resendInvitation sends invitation email to the same email address`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 1L)
            val oldInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    email = "joint@example.com",
                    propertyOwnership = propertyOwnership,
                    invitingLandlordName = invitingLandlord.name,
                )
            val mockUri = URI("https://example.com/invite/new-token")

            whenever(mockJointLandlordInvitationRepository.findById(oldInvitation.id))
                .thenReturn(Optional.of(oldInvitation))
            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
                .thenReturn(mockUri)

            invitationService.resendInvitation(oldInvitation.id, propertyOwnership, invitingLandlord)

            verify(mockInvitationEmailSender).sendEmail(eq("joint@example.com"), any())
        }

        @Test
        fun `resendInvitation returns the email address of the invitation`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 1L)
            val oldInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    email = "joint@example.com",
                    propertyOwnership = propertyOwnership,
                    invitingLandlordName = invitingLandlord.name,
                )
            val mockUri = URI("https://example.com/invite/new-token")

            whenever(mockJointLandlordInvitationRepository.findById(oldInvitation.id))
                .thenReturn(Optional.of(oldInvitation))
            whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
                .thenReturn(mockUri)

            val result = invitationService.resendInvitation(oldInvitation.id, propertyOwnership, invitingLandlord)

            assertEquals("joint@example.com", result)
        }

        @Test
        fun `resendInvitation throws NOT_FOUND when invitation does not exist`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 1L)

            whenever(mockJointLandlordInvitationRepository.findById(999L))
                .thenReturn(Optional.empty())

            val exception =
                assertThrows<ResponseStatusException> {
                    invitationService.resendInvitation(999L, propertyOwnership, invitingLandlord)
                }
            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        }

        @Test
        fun `resendInvitation throws NOT_FOUND when invitation belongs to different property`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 1L)
            val differentPropertyOwnership = MockLandlordData.createPropertyOwnership(id = 2L)
            val oldInvitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    propertyOwnership = differentPropertyOwnership,
                    invitingLandlordName = invitingLandlord.name,
                )

            whenever(mockJointLandlordInvitationRepository.findById(oldInvitation.id))
                .thenReturn(Optional.of(oldInvitation))

            val exception =
                assertThrows<ResponseStatusException> {
                    invitationService.resendInvitation(oldInvitation.id, propertyOwnership, invitingLandlord)
                }
            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        }
    }

    @Nested
    inner class GetPendingInvitationIfAuthorizedLandlord {
        @Test
        fun `getPendingInvitationIfAuthorizedLandlord returns invitation when landlord is authorized`() {
            val baseUserId = "test-base-user-id"
            val primaryLandlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId))
            val propertyOwnership = MockLandlordData.createPropertyOwnership(primaryLandlord = primaryLandlord)
            val invitation = MockJointLandlordData.createJointLandlordInvitation(propertyOwnership = propertyOwnership)
            whenever(mockJointLandlordInvitationRepository.findById(invitation.id)).thenReturn(Optional.of(invitation))

            val result = invitationService.getPendingInvitationIfAuthorizedLandlord(invitation.id, baseUserId)

            assertEquals(invitation, result)
            verify(mockJointLandlordInvitationRepository).findById(invitation.id)
        }

        @Test
        fun `getPendingInvitationIfAuthorizedLandlord throws 400 when invitation is not pending`() {
            val baseUserId = "test-base-user-id"
            val primaryLandlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId))
            val propertyOwnership = MockLandlordData.createPropertyOwnership(primaryLandlord = primaryLandlord)
            val invitation =
                MockJointLandlordData.createJointLandlordInvitation(
                    propertyOwnership = propertyOwnership,
                    createdDate = Instant.now().minus(365, ChronoUnit.DAYS),
                )
            whenever(mockJointLandlordInvitationRepository.findById(invitation.id)).thenReturn(Optional.of(invitation))

            val exception =
                assertThrows<ResponseStatusException> {
                    invitationService.getPendingInvitationIfAuthorizedLandlord(invitation.id, baseUserId)
                }

            assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        }

        @Test
        fun `getPendingInvitationIfAuthorizedLandlord throws 403 when landlord is not authorized`() {
            val primaryLandlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser("authorized-user"))
            val propertyOwnership = MockLandlordData.createPropertyOwnership(primaryLandlord = primaryLandlord)
            val invitation = MockJointLandlordData.createJointLandlordInvitation(propertyOwnership = propertyOwnership)
            whenever(mockJointLandlordInvitationRepository.findById(invitation.id)).thenReturn(Optional.of(invitation))

            val exception =
                assertThrows<ResponseStatusException> {
                    invitationService.getPendingInvitationIfAuthorizedLandlord(invitation.id, "different-user")
                }

            assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
        }

        @Test
        fun `getPendingInvitationIfAuthorizedLandlord throws 404 when invitation not found`() {
            whenever(mockJointLandlordInvitationRepository.findById(123L)).thenReturn(Optional.empty())

            val exception =
                assertThrows<ResponseStatusException> {
                    invitationService.getPendingInvitationIfAuthorizedLandlord(123L, "any-user")
                }

            assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        }
    }

    @Nested
    inner class CancelInvitation {
        @Test
        fun `cancelInvitation deletes the invitation`() {
            val invitation = MockJointLandlordData.createJointLandlordInvitation()

            invitationService.cancelInvitation(invitation)

            verify(mockJointLandlordInvitationRepository).delete(invitation)
        }
    }

    @Nested
    inner class CancelledInvitationEmailSessionMethods {
        @Test
        fun `addOrUpdateCancelledInvitationEmailInSession stores email in session`() {
            invitationService.addOrUpdateCancelledInvitationEmailInSession("test@example.com")

            verify(mockHttpSession).setAttribute(JOINT_LANDLORD_INVITATION_EMAIL_CANCELLED, "test@example.com")
        }

        @Test
        fun `getCancelledInvitationEmailFromSession returns the email`() {
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_EMAIL_CANCELLED))
                .thenReturn("test@example.com")

            assertEquals("test@example.com", invitationService.getCancelledInvitationEmailFromSession())
        }

        @Test
        fun `getCancelledInvitationEmailFromSession returns null when session has no cancelled email`() {
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_EMAIL_CANCELLED)).thenReturn(null)

            assertNull(invitationService.getCancelledInvitationEmailFromSession())
        }
    }

    @Nested
    inner class GetInvitationFromToken {
        @Test
        fun `getInvitationFromToken returns invitation when found`() {
            // Arrange
            val token = UUID.randomUUID()
            val mockInvitation = mock<JointLandlordInvitation>()
            whenever(mockJointLandlordInvitationRepository.findByToken(token)).thenReturn(mockInvitation)

            // Act
            val result = invitationService.getInvitationFromToken(token.toString())

            // Assert
            assertEquals(mockInvitation, result)
        }

        @Test
        fun `getInvitationFromToken throws error when not found`() {
            // Arrange
            val token = UUID.randomUUID()
            whenever(mockJointLandlordInvitationRepository.findByToken(token)).thenReturn(null)

            // Act & Assert
            assertThrows<EntityNotFoundException> {
                invitationService.getInvitationFromToken(token.toString())
            }
        }
    }

    @Nested
    inner class GetInvitationForJourney {
        @Test
        fun `getInvitationForJourney returns invitation for valid journey`() {
            // Arrange
            val journeyId = "test-journey"
            val token = UUID.randomUUID()
            val pairs = mutableListOf(Pair(journeyId, token.toString()))
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(pairs)
            val mockInvitation = mock<JointLandlordInvitation>()
            whenever(mockJointLandlordInvitationRepository.findByToken(token)).thenReturn(mockInvitation)

            // Act
            val result = invitationService.getInvitationForJourney(journeyId)

            // Assert
            assertEquals(mockInvitation, result)
        }

        @Test
        fun `getInvitationForJourney throws when journey id not in session`() {
            // Arrange
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(null)

            // Act & Assert
            assertThrows<PrsdbWebException> {
                invitationService.getInvitationForJourney("nonexistent")
            }
        }

        @Test
        fun `getInvitationForJourney throws when invitation not found in database`() {
            // Arrange
            val journeyId = "test-journey"
            val token = UUID.randomUUID()
            val pairs = mutableListOf(Pair(journeyId, token.toString()))
            whenever(mockHttpSession.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS))
                .thenReturn(pairs)
            whenever(mockJointLandlordInvitationRepository.findByToken(token)).thenReturn(null)

            // Act & Assert
            assertThrows<EntityNotFoundException> {
                invitationService.getInvitationForJourney(journeyId)
            }
        }
    }

    @Nested
    inner class StoreLastAcceptedPropertyInSession {
        @Test
        fun `storeLastAcceptedPropertyInSession stores address and ownership id pair in session`() {
            val address = "1 Test Street\nTest Town\nAB1 2CD"
            val propertyOwnershipId = 42L

            invitationService.storeLastAcceptedPropertyInSession(address, propertyOwnershipId)

            verify(mockHttpSession).setAttribute(ACCEPTED_JOINT_LANDLORD_PROPERTY_DETAILS, Pair(address, propertyOwnershipId))
        }
    }

    @Nested
    inner class GetLastAcceptedPropertyFromSession {
        @Test
        fun `getLastAcceptedPropertyFromSession returns pair when present`() {
            val address = "1 Test Street\nTest Town\nAB1 2CD"
            val propertyOwnershipId = 42L
            whenever(mockHttpSession.getAttribute(ACCEPTED_JOINT_LANDLORD_PROPERTY_DETAILS)).thenReturn(Pair(address, propertyOwnershipId))

            val result = invitationService.getLastAcceptedPropertyFromSession()

            assertEquals(Pair(address, propertyOwnershipId), result)
        }

        @Test
        fun `getLastAcceptedPropertyFromSession returns null when not present`() {
            whenever(mockHttpSession.getAttribute(ACCEPTED_JOINT_LANDLORD_PROPERTY_DETAILS)).thenReturn(null)

            assertNull(invitationService.getLastAcceptedPropertyFromSession())
        }
    }
}
