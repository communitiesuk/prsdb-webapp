package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.CancelDelegationJointLandlordNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.CancelDelegationLandlordConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.CancelDelegationLettingAgentNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.DelegateToLettingAgentConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.DelegateToLettingAgentInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.DelegateToLettingAgentInvitationWithDeadlineEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordDelegateToLettingAgentNotificationEmail
import uk.gov.communities.prsdb.webapp.services.CurrentUserService.CurrentUserDetails
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class DelegateToLettingAgentEmailServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockConfirmationEmailService: EmailNotificationService<DelegateToLettingAgentConfirmationEmail>

    @Mock
    private lateinit var mockNotificationEmailService: EmailNotificationService<JointLandlordDelegateToLettingAgentNotificationEmail>

    @Mock
    private lateinit var mockCancelLandlordConfirmationEmailService: EmailNotificationService<CancelDelegationLandlordConfirmationEmail>

    @Mock
    private lateinit var mockCancelJointLandlordNotificationEmailService:
        EmailNotificationService<CancelDelegationJointLandlordNotificationEmail>

    @Mock
    private lateinit var mockCancelLettingAgentNotificationEmailService:
        EmailNotificationService<CancelDelegationLettingAgentNotificationEmail>

    @Mock
    private lateinit var mockInvitationEmailService: EmailNotificationService<DelegateToLettingAgentInvitationEmail>

    @Mock
    private lateinit var mockInvitationWithDeadlineEmailService:
        EmailNotificationService<DelegateToLettingAgentInvitationWithDeadlineEmail>

    @Mock
    private lateinit var mockCurrentUserService: CurrentUserService

    private val propertyOwnershipId = 123L
    private val agentEmail = "agent@example.com"

    private lateinit var emailService: DelegateToLettingAgentEmailService

    @BeforeEach
    fun setUp() {
        emailService =
            DelegateToLettingAgentEmailService(
                mockPropertyOwnershipService,
                mockUserToLandlordService,
                mockAbsoluteUrlProvider,
                mockConfirmationEmailService,
                mockNotificationEmailService,
                mockCancelLandlordConfirmationEmailService,
                mockCancelJointLandlordNotificationEmailService,
                mockCancelLettingAgentNotificationEmailService,
                mockInvitationEmailService,
                mockInvitationWithDeadlineEmailService,
                mockCurrentUserService,
            )
    }

    @Test
    fun `sendDelegationEmails sends confirmation email to the current user`() {
        val baseUserId = "acting-user"
        val actor =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                email = "actor@example.com",
            )
        val other = MockLandlordData.createIndividualLandlord(email = "other@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, landlords = mutableSetOf(actor, other))
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(actor)
        whenever(mockCurrentUserService.getCurrentUserDetails())
            .thenReturn(CurrentUserDetails(name = "Current User", email = "current-user@example.com"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyOwnershipId)).thenReturn(URI("http://property"))

        emailService.sendDelegationEmailToLandlords(propertyOwnershipId, agentEmail)

        verify(mockConfirmationEmailService).sendEmail(
            eq("current-user@example.com"),
            argThat<DelegateToLettingAgentConfirmationEmail> {
                this.recipientName == "Current User" && this.lettingAgentEmail == agentEmail
            },
        )
    }

    @Test
    fun `sendDelegationEmails sends notifications to joint landlords but not the acting landlord`() {
        val baseUserId = "acting-user"
        val actor =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                email = "actor@example.com",
            )
        val other = MockLandlordData.createIndividualLandlord(name = "Lois", email = "other@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, landlords = mutableSetOf(actor, other))
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(actor)
        whenever(mockCurrentUserService.getCurrentUserDetails())
            .thenReturn(CurrentUserDetails(name = "Current User", email = "current-user@example.com"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyOwnershipId)).thenReturn(URI("http://property"))

        emailService.sendDelegationEmailToLandlords(propertyOwnershipId, agentEmail)

        verify(mockNotificationEmailService).sendEmailToLandlord(
            eq(other),
            argThat<JointLandlordDelegateToLettingAgentNotificationEmail> {
                this.recipientName == other.name && this.lettingAgentEmail == agentEmail &&
                    this.propertyRecordUrl == "http://property"
            },
        )
        verify(mockNotificationEmailService, never()).sendEmailToLandlord(eq(actor), any())
    }

    @Test
    fun `sendDelegationEmails does not send notification when no joint landlords`() {
        val baseUserId = "acting-user"
        val actor =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                email = "actor@example.com",
            )
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, landlords = mutableSetOf(actor))
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(actor)
        whenever(mockCurrentUserService.getCurrentUserDetails())
            .thenReturn(CurrentUserDetails(name = "Current User", email = "current-user@example.com"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyOwnershipId)).thenReturn(URI("http://property"))

        emailService.sendDelegationEmailToLandlords(propertyOwnershipId, agentEmail)

        verify(mockNotificationEmailService, never()).sendEmailToLandlord(any(), any())
    }

    @Test
    fun `sendDelegationEmailToLettingAgent sends no-deadline template when deadlineDate is null`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        val token = UUID.randomUUID()
        whenever(mockAbsoluteUrlProvider.buildLettingAgentPropertyDetailsUri(token))
            .thenReturn(URI("https://example.com/landlord/letting-agent/property-details/$token"))

        emailService.sendDelegationEmailToLettingAgent(propertyOwnership, "Wallis Smith", agentEmail, invitationToken = token)

        verify(mockInvitationEmailService).sendEmail(
            eq(agentEmail),
            argThat<DelegateToLettingAgentInvitationEmail> {
                this.landlordName == "Wallis Smith" &&
                    this.invitationLink == "https://example.com/landlord/letting-agent/property-details/$token"
            },
        )
        verify(mockInvitationWithDeadlineEmailService, never()).sendEmail(any(), any())
    }

    @Test
    fun `sendDelegationEmailToLettingAgent sends with-deadline template when deadlineDate is provided`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        val token = UUID.randomUUID()
        whenever(mockAbsoluteUrlProvider.buildLettingAgentPropertyDetailsUri(token))
            .thenReturn(URI("https://example.com/landlord/letting-agent/property-details/$token"))

        emailService.sendDelegationEmailToLettingAgent(
            propertyOwnership,
            "Wallis Smith",
            agentEmail,
            "13 June 2026",
            invitationToken = token,
        )

        verify(mockInvitationWithDeadlineEmailService).sendEmail(
            eq(agentEmail),
            argThat<DelegateToLettingAgentInvitationWithDeadlineEmail> {
                this.landlordName == "Wallis Smith" && this.deadlineDate == "13 June 2026" &&
                    this.invitationLink == "https://example.com/landlord/letting-agent/property-details/$token"
            },
        )
        verify(mockInvitationEmailService, never()).sendEmail(any(), any())
    }

    @Test
    fun `sendCancellationEmails sends confirmation email to the current user with correct details`() {
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 5, landlords = mutableSetOf(landlord))
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
        whenever(mockCurrentUserService.getCurrentUserDetails())
            .thenReturn(CurrentUserDetails(name = "Current User", email = "current-user@example.com"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(5)).thenReturn(URI("https://example.com/property/5"))

        emailService.sendCancellationEmails(propertyOwnership, agentEmail)

        val captor = argumentCaptor<CancelDelegationLandlordConfirmationEmail>()
        verify(mockCancelLandlordConfirmationEmailService).sendEmail(eq("current-user@example.com"), captor.capture())
        assertEquals("Current User", captor.firstValue.landlordName)
        assertEquals(agentEmail, captor.firstValue.lettingAgentEmail)
        assertEquals(propertyOwnership.address.toMultiLineAddress(), captor.firstValue.propertyAddress)
        assertEquals("https://example.com/property/5", captor.firstValue.propertyRecordUrl)
    }

    @Test
    fun `sendCancellationEmails notifies joint landlords through sendEmailToLandlord`() {
        val actingLandlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val jointLandlord = MockLandlordData.createIndividualLandlord(name = "Bob", email = "bob@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = 5,
                landlords = mutableSetOf(actingLandlord, jointLandlord),
            )
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(actingLandlord)
        whenever(mockCurrentUserService.getCurrentUserDetails())
            .thenReturn(CurrentUserDetails(name = "Current User", email = "current-user@example.com"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(5)).thenReturn(URI("https://example.com/property/5"))

        emailService.sendCancellationEmails(propertyOwnership, agentEmail)

        val captor = argumentCaptor<CancelDelegationJointLandlordNotificationEmail>()
        verify(mockCancelJointLandlordNotificationEmailService).sendEmailToLandlord(eq(jointLandlord), captor.capture())
        assertEquals("Bob", captor.firstValue.jointLandlordName)
        assertEquals(agentEmail, captor.firstValue.lettingAgentEmail)
        verify(mockCancelJointLandlordNotificationEmailService, never()).sendEmailToLandlord(eq(actingLandlord), any())
    }

    @Test
    fun `sendCancellationEmails sends notification to letting agent`() {
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 5, landlords = mutableSetOf(landlord))
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
        whenever(mockCurrentUserService.getCurrentUserDetails())
            .thenReturn(CurrentUserDetails(name = "Current User", email = "current-user@example.com"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(5)).thenReturn(URI("https://example.com/property/5"))

        emailService.sendCancellationEmails(propertyOwnership, agentEmail)

        val captor = argumentCaptor<CancelDelegationLettingAgentNotificationEmail>()
        verify(mockCancelLettingAgentNotificationEmailService).sendEmail(eq(agentEmail), captor.capture())
        assertEquals(propertyOwnership.address.toMultiLineAddress(), captor.firstValue.propertyAddress)
        assertEquals(propertyOwnership.address.singleLineAddress, captor.firstValue.singleLineAddress)
    }

    @Test
    fun `sendCancellationEmails does not send joint landlord email when there are no other landlords`() {
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 5, landlords = mutableSetOf(landlord))
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
        whenever(mockCurrentUserService.getCurrentUserDetails())
            .thenReturn(CurrentUserDetails(name = "Current User", email = "current-user@example.com"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(5)).thenReturn(URI("https://example.com/property/5"))

        emailService.sendCancellationEmails(propertyOwnership, agentEmail)

        verify(mockCancelJointLandlordNotificationEmailService, never()).sendEmailToLandlord(any(), any())
    }

    @Test
    fun `sendLettingAgentCancellationEmail sends only the letting agent notification email`() {
        val landlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = 5, landlords = mutableSetOf(landlord))

        emailService.sendLettingAgentCancellationEmail(propertyOwnership, agentEmail)

        val captor = argumentCaptor<CancelDelegationLettingAgentNotificationEmail>()
        verify(mockCancelLettingAgentNotificationEmailService).sendEmail(eq(agentEmail), captor.capture())
        assertEquals(propertyOwnership.address.toMultiLineAddress(), captor.firstValue.propertyAddress)
        assertEquals(propertyOwnership.address.singleLineAddress, captor.firstValue.singleLineAddress)
        verify(mockCancelLandlordConfirmationEmailService, never()).sendEmail(any(), any())
        verify(mockCancelJointLandlordNotificationEmailService, never()).sendEmail(any(), any())
    }
}
