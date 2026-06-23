package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.SwapToIndividualNudgeEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import java.time.Instant
import kotlin.test.assertEquals

class SwapToIndividualNudgeEmailServiceTests {
    private lateinit var mockInvitationRepository: JointLandlordInvitationRepository
    private lateinit var mockNudgeEmailNotificationService: EmailNotificationService<SwapToIndividualNudgeEmail>
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var nudgeService: SwapToIndividualNudgeEmailServiceImplFlagOn

    @BeforeEach
    fun setup() {
        mockInvitationRepository = mock()
        mockNudgeEmailNotificationService = mock()
        mockAbsoluteUrlProvider = mock()
        nudgeService =
            SwapToIndividualNudgeEmailServiceImplFlagOn(
                mockInvitationRepository,
                mockNudgeEmailNotificationService,
                mockAbsoluteUrlProvider,
            )
    }

    @Test
    fun `sendNudgeEmailIfApplicable sends email when property is marked joint with sole landlord and no pending invitations`() {
        val landlord = MockLandlordData.createLandlord(name = "Alice", email = "alice@example.com")
        val address = MockLandlordData.createAddress(singleLineAddress = "10 High Street, London, SW1A 1AA")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                address = address,
                markedJointLandlord = true,
            )
        val propertyRecordUri = URI("https://example.com/landlord/property/1")

        whenever(mockInvitationRepository.findByRegisteredOwnership(propertyOwnership))
            .thenReturn(emptyList())
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(any()))
            .thenReturn(propertyRecordUri)

        nudgeService.sendNudgeEmailIfApplicable(propertyOwnership)

        val emailCaptor = argumentCaptor<SwapToIndividualNudgeEmail>()
        verify(mockNudgeEmailNotificationService).sendEmail(eq("alice@example.com"), emailCaptor.capture())

        val sentEmail = emailCaptor.firstValue
        assertEquals("Alice", sentEmail.recipientName)
        assertEquals("10 High Street\nLondon\nSW1A 1AA", sentEmail.propertyAddress)
        assertEquals(propertyRecordUri.toString(), sentEmail.propertyRecordUrl)
    }

    @Test
    fun `sendNudgeEmailIfApplicable does not send email when property is not marked as joint`() {
        val landlord = MockLandlordData.createLandlord()
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                markedJointLandlord = false,
            )

        nudgeService.sendNudgeEmailIfApplicable(propertyOwnership)

        verify(mockNudgeEmailNotificationService, never()).sendEmail(any(), any())
    }

    @Test
    fun `sendNudgeEmailIfApplicable does not send email when property has more than one landlord`() {
        val primaryLandlord = MockLandlordData.createLandlord()
        val otherLandlord = MockLandlordData.createLandlord()
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = primaryLandlord,
                otherLandlords = mutableSetOf(otherLandlord),
                markedJointLandlord = true,
            )

        nudgeService.sendNudgeEmailIfApplicable(propertyOwnership)

        verify(mockNudgeEmailNotificationService, never()).sendEmail(any(), any())
    }

    @Test
    fun `sendNudgeEmailIfApplicable does not send email when pending invitations exist`() {
        val landlord = MockLandlordData.createLandlord()
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                markedJointLandlord = true,
            )
        val pendingInvitation =
            MockJointLandlordData.createJointLandlordInvitation(
                propertyOwnership = propertyOwnership,
                createdDate = Instant.now(),
            )

        whenever(mockInvitationRepository.findByRegisteredOwnership(propertyOwnership))
            .thenReturn(listOf(pendingInvitation))

        nudgeService.sendNudgeEmailIfApplicable(propertyOwnership)

        verify(mockNudgeEmailNotificationService, never()).sendEmail(any(), any())
    }

    @Test
    fun `sendNudgeEmailIfApplicable does not send email when expired invitations have not had expiry email sent`() {
        val landlord = MockLandlordData.createLandlord()
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                markedJointLandlord = true,
            )
        val expiredInvitation =
            MockJointLandlordData.createJointLandlordInvitation(
                propertyOwnership = propertyOwnership,
                createdDate = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS),
            )

        whenever(mockInvitationRepository.findByRegisteredOwnership(propertyOwnership))
            .thenReturn(listOf(expiredInvitation))

        nudgeService.sendNudgeEmailIfApplicable(propertyOwnership)

        verify(mockNudgeEmailNotificationService, never()).sendEmail(any(), any())
    }

    @Test
    fun `sendNudgeEmailIfApplicable sends email when only processed expired invitations exist`() {
        val landlord = MockLandlordData.createLandlord(name = "Bob", email = "bob@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = landlord,
                markedJointLandlord = true,
            )
        val expiredInvitation =
            MockJointLandlordData.createJointLandlordInvitation(
                propertyOwnership = propertyOwnership,
                createdDate = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS),
            )
        expiredInvitation.markAsExpiredEmailSent()
        val propertyRecordUri = URI("https://example.com/landlord/property/1")

        whenever(mockInvitationRepository.findByRegisteredOwnership(propertyOwnership))
            .thenReturn(listOf(expiredInvitation))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(any()))
            .thenReturn(propertyRecordUri)

        nudgeService.sendNudgeEmailIfApplicable(propertyOwnership)

        verify(mockNudgeEmailNotificationService).sendEmail(eq("bob@example.com"), any())
    }

    @Test
    fun `flag-off implementation does nothing`() {
        val flagOff = SwapToIndividualNudgeEmailServiceImplFlagOff()
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(markedJointLandlord = true)

        flagOff.sendNudgeEmailIfApplicable(propertyOwnership)

        verify(mockNudgeEmailNotificationService, never()).sendEmail(any(), any())
    }
}
