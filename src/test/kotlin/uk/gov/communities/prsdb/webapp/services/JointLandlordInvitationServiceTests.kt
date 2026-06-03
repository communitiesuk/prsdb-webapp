package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail
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

    @Nested
    inner class GetInvitationHasExpired {
        private lateinit var mockInvitation: JointLandlordInvitation

        @BeforeEach
        fun setup() {
            mockInvitation = mock()
        }

        @Test
        fun `returns false when invitation was created today`() {
            whenever(mockInvitation.createdDate).thenReturn(Instant.now())

            val result = invitationService.getInvitationHasExpired(mockInvitation)

            assertFalse(result)
        }

        @Test
        fun `returns false when invitation was created exactly the lifetime in days ago`() {
            val createdDate = Instant.now().minus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS.toLong(), ChronoUnit.DAYS)
            whenever(mockInvitation.createdDate).thenReturn(createdDate)

            val result = invitationService.getInvitationHasExpired(mockInvitation)

            assertFalse(result)
        }

        @Test
        fun `returns true when invitation was created more than the lifetime in days ago`() {
            val createdDate = Instant.now().minus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS.toLong() + 1, ChronoUnit.DAYS)
            whenever(mockInvitation.createdDate).thenReturn(createdDate)

            val result = invitationService.getInvitationHasExpired(mockInvitation)

            assertTrue(result)
        }
    }
}
