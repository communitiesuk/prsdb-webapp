package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationExpiryEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit

class JointLandlordInvitationExpiryServiceTests {
    private lateinit var mockJointLandlordInvitationRepository: JointLandlordInvitationRepository
    private lateinit var mockExpiryEmailNotificationService: EmailNotificationService<JointLandlordInvitationExpiryEmail>
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var expiryService: JointLandlordInvitationExpiryServiceImplFlagOn

    @BeforeEach
    fun setup() {
        mockJointLandlordInvitationRepository = mock()
        mockExpiryEmailNotificationService = mock()
        mockAbsoluteUrlProvider = mock()
        expiryService =
            JointLandlordInvitationExpiryServiceImplFlagOn(
                mockJointLandlordInvitationRepository,
                mockExpiryEmailNotificationService,
                mockAbsoluteUrlProvider,
            )
    }

    @Test
    fun `expirePendingInvitations queries the repository with a cutoff of 28 days ago`() {
        whenever(mockJointLandlordInvitationRepository.findAllByExpiredFalseAndCreatedDateBefore(any()))
            .thenReturn(emptyList())

        val beforeCall = Instant.now()
        expiryService.expirePendingInvitations()
        val afterCall = Instant.now()

        val cutoffCaptor = argumentCaptor<Instant>()
        verify(mockJointLandlordInvitationRepository).findAllByExpiredFalseAndCreatedDateBefore(cutoffCaptor.capture())

        val expectedCutoffLowerBound =
            beforeCall.minus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS.toLong(), ChronoUnit.HOURS)
        val expectedCutoffUpperBound =
            afterCall.minus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS.toLong(), ChronoUnit.HOURS)
        val actualCutoff = cutoffCaptor.firstValue

        assert(actualCutoff in expectedCutoffLowerBound..expectedCutoffUpperBound) {
            "Cutoff $actualCutoff was outside expected window [$expectedCutoffLowerBound, $expectedCutoffUpperBound]"
        }
    }

    @Test
    fun `expirePendingInvitations sends expiry email to the primary landlord for each expired invitation`() {
        val primaryLandlord = MockLandlordData.createLandlord(name = "Lois", email = "lois@example.com")
        val address = MockLandlordData.createAddress(singleLineAddress = "Flat 1, 11 Elm Drive, London, NW8 2DK")
        val propertyOwnership = MockLandlordData.createPropertyOwnership(primaryLandlord = primaryLandlord, address = address)
        val invitation =
            MockJointLandlordData.createJointLandlordInvitation(
                email = "very-real-email@example.com",
                propertyOwnership = propertyOwnership,
            )
        val propertyRecordUri = URI("https://example.com/landlord/property/1")

        whenever(mockJointLandlordInvitationRepository.findAllByExpiredFalseAndCreatedDateBefore(any()))
            .thenReturn(listOf(invitation))
        whenever(mockAbsoluteUrlProvider.buildLandlordPropertyDetailsUri(any()))
            .thenReturn(propertyRecordUri)

        expiryService.expirePendingInvitations()

        val emailModelCaptor = argumentCaptor<JointLandlordInvitationExpiryEmail>()
        verify(mockExpiryEmailNotificationService).sendEmail(eq("lois@example.com"), emailModelCaptor.capture())

        val sentEmail = emailModelCaptor.firstValue
        assertEquals("Lois", sentEmail.recipientName)
        assertEquals("very-real-email@example.com", sentEmail.invitedEmail)
        assertEquals("Flat 1\n11 Elm Drive\nLondon\nNW8 2DK", sentEmail.propertyAddress)
        assertEquals(propertyRecordUri, sentEmail.propertyRecordUri)
        assertEquals(28, sentEmail.expiryDays)
    }

    @Test
    fun `expirePendingInvitations sends one email per expired invitation`() {
        val invitations =
            listOf(
                MockJointLandlordData.createJointLandlordInvitation(id = 1, email = "first@example.com"),
                MockJointLandlordData.createJointLandlordInvitation(id = 2, email = "second@example.com"),
                MockJointLandlordData.createJointLandlordInvitation(id = 3, email = "third@example.com"),
            )

        whenever(mockJointLandlordInvitationRepository.findAllByExpiredFalseAndCreatedDateBefore(any()))
            .thenReturn(invitations)
        whenever(mockAbsoluteUrlProvider.buildLandlordPropertyDetailsUri(any()))
            .thenReturn(URI("https://example.com/landlord/property/1"))

        expiryService.expirePendingInvitations()

        verify(mockExpiryEmailNotificationService, times(3)).sendEmail(any(), any())
    }

    @Test
    fun `expirePendingInvitations marks a non-expired invitation as expired`() {
        val invitation = MockJointLandlordData.createJointLandlordInvitation(id = 1, email = "test@example.com")
        assert(!invitation.expired) { "Expected invitation to start as non-expired" }

        whenever(mockJointLandlordInvitationRepository.findAllByExpiredFalseAndCreatedDateBefore(any()))
            .thenReturn(listOf(invitation))
        whenever(mockAbsoluteUrlProvider.buildLandlordPropertyDetailsUri(any()))
            .thenReturn(URI("https://example.com/landlord/property/1"))

        expiryService.expirePendingInvitations()

        assert(invitation.expired) { "Expected invitation to be marked as expired" }
        verify(mockJointLandlordInvitationRepository).save(invitation)
    }

    @Test
    fun `expirePendingInvitations marks each invitation as expired and saves it after sending the email`() {
        val invitations =
            listOf(
                MockJointLandlordData.createJointLandlordInvitation(id = 1, email = "first@example.com"),
                MockJointLandlordData.createJointLandlordInvitation(id = 2, email = "second@example.com"),
            )

        whenever(mockJointLandlordInvitationRepository.findAllByExpiredFalseAndCreatedDateBefore(any()))
            .thenReturn(invitations)
        whenever(mockAbsoluteUrlProvider.buildLandlordPropertyDetailsUri(any()))
            .thenReturn(URI("https://example.com/landlord/property/1"))

        expiryService.expirePendingInvitations()

        invitations.forEach { invitation ->
            assert(invitation.expired) { "Expected invitation ${invitation.id} to be marked as expired" }
            verify(mockJointLandlordInvitationRepository).save(invitation)
        }
    }

    @Test
    fun `expirePendingInvitations does nothing when there are no expired invitations`() {
        whenever(mockJointLandlordInvitationRepository.findAllByExpiredFalseAndCreatedDateBefore(any()))
            .thenReturn(emptyList())

        expiryService.expirePendingInvitations()

        verify(mockExpiryEmailNotificationService, never()).sendEmail(any(), any())
        verify(mockJointLandlordInvitationRepository, never()).save(any<JointLandlordInvitation>())
    }

    @Test
    fun `expirePendingInvitations continues processing and does not delete the failed invitation when an email send fails`() {
        val failingInvitation = MockJointLandlordData.createJointLandlordInvitation(id = 1, email = "fail@example.com")
        val succeedingInvitation = MockJointLandlordData.createJointLandlordInvitation(id = 2, email = "ok@example.com")

        whenever(mockJointLandlordInvitationRepository.findAllByExpiredFalseAndCreatedDateBefore(any()))
            .thenReturn(listOf(failingInvitation, succeedingInvitation))
        whenever(mockAbsoluteUrlProvider.buildLandlordPropertyDetailsUri(any()))
            .thenReturn(URI("https://example.com/landlord/property/1"))
        whenever(mockExpiryEmailNotificationService.sendEmail(any(), any()))
            .thenThrow(PersistentEmailSendException("boom"))
            .thenAnswer { /* succeed on the second call */ }

        expiryService.expirePendingInvitations()

        verify(mockJointLandlordInvitationRepository, never()).save(failingInvitation)
        assert(!failingInvitation.expired) { "Expected failing invitation to not be marked as expired" }
        verify(mockJointLandlordInvitationRepository).save(succeedingInvitation)
        assert(succeedingInvitation.expired) { "Expected succeeding invitation to be marked as expired" }
    }

    @Test
    fun `flag-off implementation does nothing`() {
        val flagOff = JointLandlordInvitationExpiryServiceImplFlagOff()

        flagOff.expirePendingInvitations()

        // No dependencies, no side effects - nothing to verify beyond not throwing.
    }
}
