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
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

class JointLandlordInvitationServiceTests {
    private lateinit var mockJointLandlordInvitationRepository: JointLandlordInvitationRepository
    private lateinit var mockEmailNotificationService: EmailNotificationService<JointLandlordInvitationEmail>
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var invitationService: JointLandlordInvitationService

    @BeforeEach
    fun setup() {
        mockJointLandlordInvitationRepository = mock()
        mockEmailNotificationService = mock()
        mockAbsoluteUrlProvider = mock()
        invitationService =
            JointLandlordInvitationService(
                mockJointLandlordInvitationRepository,
                mockEmailNotificationService,
                mockAbsoluteUrlProvider,
            )
    }

    @Test
    fun `sendInvitationEmails creates invitation tokens for each email address`() {
        val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com", "landlord3@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val mockToken = "test-token-123"
        val mockUri = URI("https://example.com/invite/$mockToken")

        whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
            .thenReturn(mockUri)

        invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership)

        verify(mockJointLandlordInvitationRepository, times(3)).save(any())
    }

    @Test
    fun `sendInvitationEmails sends an email to each joint landlord`() {
        val jointLandlordEmails = listOf("landlord1@example.com", "landlord2@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val mockUri = URI("https://example.com/invite/test-token")

        whenever(mockAbsoluteUrlProvider.buildJointLandlordInvitationUri(any()))
            .thenReturn(mockUri)

        invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership)

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

        invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership)

        val emailModelCaptor = argumentCaptor<JointLandlordInvitationEmail>()
        verify(mockEmailNotificationService)
            .sendEmail(eq("landlord1@example.com"), emailModelCaptor.capture())

        assertEquals("John Smith", emailModelCaptor.firstValue.senderName)
        assertEquals("123 Test Street, London, SW1A 1AA", emailModelCaptor.firstValue.propertyAddress)
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

        invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership)

        verify(mockJointLandlordInvitationRepository, times(2)).save(any())
        verify(mockAbsoluteUrlProvider, times(2)).buildJointLandlordInvitationUri(any())
    }

    @Test
    fun `sendInvitationEmails handles empty list without error`() {
        val jointLandlordEmails = emptyList<String>()
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        invitationService.sendInvitationEmails(jointLandlordEmails, propertyOwnership)

        verify(mockJointLandlordInvitationRepository, times(0)).save(any())
        verify(mockEmailNotificationService, times(0)).sendEmail(any(), any())
    }
}
