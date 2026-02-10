package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import kotlin.test.assertEquals

class JointLandlordInvitationEmailSenderTests {
    private lateinit var jointLandlordInvitationService: JointLandlordInvitationService
    private lateinit var emailNotificationService: EmailNotificationService<JointLandlordInvitationEmail>
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var jointLandlordInvitationEmailSender: JointLandlordInvitationEmailSender

    @BeforeEach
    fun setup() {
        jointLandlordInvitationService = mock()
        emailNotificationService = mock()
        absoluteUrlProvider = mock()
        jointLandlordInvitationEmailSender =
            JointLandlordInvitationEmailSender(
                jointLandlordInvitationService,
                emailNotificationService,
                absoluteUrlProvider,
            )
    }

    @Test
    fun `sendInvitationEmails creates invitation tokens for each email address`() {
        val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com", "landlord3@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val mockToken = "test-token-123"
        val mockUri = URI("https://example.com/invite/$mockToken")

        whenever(jointLandlordInvitationService.createInvitationToken(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn(mockToken)
        whenever(absoluteUrlProvider.buildJointLandlordInvitationUri(mockToken))
            .thenReturn(mockUri)

        jointLandlordInvitationEmailSender.sendInvitationEmails(jointLandlordEmails, propertyOwnership)

        verify(jointLandlordInvitationService, times(3))
            .createInvitationToken(org.mockito.kotlin.any(), org.mockito.kotlin.eq(propertyOwnership))
    }

    @Test
    fun `sendInvitationEmails sends an email to each joint landlord`() {
        val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val mockToken = "test-token-456"
        val mockUri = URI("https://example.com/invite/$mockToken")

        whenever(jointLandlordInvitationService.createInvitationToken(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn(mockToken)
        whenever(absoluteUrlProvider.buildJointLandlordInvitationUri(mockToken))
            .thenReturn(mockUri)

        jointLandlordInvitationEmailSender.sendInvitationEmails(jointLandlordEmails, propertyOwnership)

        val emailCaptor = argumentCaptor<String>()
        verify(emailNotificationService, times(2))
            .sendEmail(emailCaptor.capture(), org.mockito.kotlin.any())

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
        val mockToken = "test-token-789"
        val mockUri = URI("https://example.com/invite/$mockToken")

        whenever(jointLandlordInvitationService.createInvitationToken(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn(mockToken)
        whenever(absoluteUrlProvider.buildJointLandlordInvitationUri(mockToken))
            .thenReturn(mockUri)

        jointLandlordInvitationEmailSender.sendInvitationEmails(jointLandlordEmails, propertyOwnership)

        val emailModelCaptor = argumentCaptor<JointLandlordInvitationEmail>()
        verify(emailNotificationService)
            .sendEmail(org.mockito.kotlin.eq("landlord1@example.com"), emailModelCaptor.capture())

        assertEquals("John Smith", emailModelCaptor.firstValue.senderName)
        assertEquals("123 Test Street, London, SW1A 1AA", emailModelCaptor.firstValue.propertyAddress)
        assertEquals(mockUri, emailModelCaptor.firstValue.invitationUri)
    }

    @Test
    fun `sendInvitationEmails creates unique tokens for each invitation`() {
        val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val mockToken1 = "token-1"
        val mockToken2 = "token-2"
        val mockUri1 = URI("https://example.com/invite/$mockToken1")
        val mockUri2 = URI("https://example.com/invite/$mockToken2")

        whenever(jointLandlordInvitationService.createInvitationToken("landlord1@example.com", propertyOwnership))
            .thenReturn(mockToken1)
        whenever(jointLandlordInvitationService.createInvitationToken("landlord2@example.com", propertyOwnership))
            .thenReturn(mockToken2)
        whenever(absoluteUrlProvider.buildJointLandlordInvitationUri(mockToken1))
            .thenReturn(mockUri1)
        whenever(absoluteUrlProvider.buildJointLandlordInvitationUri(mockToken2))
            .thenReturn(mockUri2)

        jointLandlordInvitationEmailSender.sendInvitationEmails(jointLandlordEmails, propertyOwnership)

        verify(jointLandlordInvitationService).createInvitationToken("landlord1@example.com", propertyOwnership)
        verify(jointLandlordInvitationService).createInvitationToken("landlord2@example.com", propertyOwnership)
        verify(absoluteUrlProvider).buildJointLandlordInvitationUri(mockToken1)
        verify(absoluteUrlProvider).buildJointLandlordInvitationUri(mockToken2)
    }

    @Test
    fun `sendInvitationEmails handles empty list without error`() {
        val jointLandlordEmails = emptyList<String>()
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        jointLandlordInvitationEmailSender.sendInvitationEmails(jointLandlordEmails, propertyOwnership)

        verify(jointLandlordInvitationService, times(0))
            .createInvitationToken(org.mockito.kotlin.any(), org.mockito.kotlin.any())
        verify(emailNotificationService, times(0))
            .sendEmail(org.mockito.kotlin.any(), org.mockito.kotlin.any())
    }
}
