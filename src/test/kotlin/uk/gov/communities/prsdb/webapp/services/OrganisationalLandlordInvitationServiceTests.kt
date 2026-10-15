package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.ACCEPTED_ORGANISATIONAL_LANDLORD_INVITATION_EMAIL
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole.ADMIN
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole.EDITOR
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordInvitationAcceptedEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrganisationalLandlordInvitation
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class OrganisationalLandlordInvitationServiceTests {
    @Mock
    private lateinit var mockInvitationRepository: OrganisationalLandlordInvitationRepository

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @Mock
    private lateinit var mockOrganisationalLandlordUserService: OrganisationalLandlordUserService

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockInvitationEmailSender: EmailNotificationService<OrganisationalLandlordInvitationEmail>

    @Mock
    private lateinit var mockAcceptedEmailSender: EmailNotificationService<OrganisationalLandlordInvitationAcceptedEmail>

    @Mock
    private lateinit var mockPrsdbUserService: PrsdbUserService

    private lateinit var invitationService: OrganisationalLandlordInvitationService

    private val journeyId = "test-journey-id"
    private val token = UUID.fromString("1234abcd-5678-abcd-1234-567abcd2222a")
    private val acceptingUserId = "signed-in-user"
    private val inviteeName = "Jane Smith"
    private val inviteeEmail = "invitee@example.com"

    @BeforeEach
    fun setup() {
        invitationService =
            OrganisationalLandlordInvitationService(
                mockInvitationRepository,
                mockHttpSession,
                mockOrganisationalLandlordUserService,
                mockAbsoluteUrlProvider,
                mockInvitationEmailSender,
                mockAcceptedEmailSender,
                mockPrsdbUserService,
            )
    }

    @Test
    fun `storeAcceptedInvitationEmailInSession records the submitted email`() {
        // Act
        invitationService.storeAcceptedInvitationEmailInSession("invitee@example.com")

        // Assert
        verify(mockHttpSession).setAttribute(ACCEPTED_ORGANISATIONAL_LANDLORD_INVITATION_EMAIL, "invitee@example.com")
    }

    @Test
    fun `getAcceptedInvitationEmailFromSession returns null without an accepted invitation`() {
        // Act & Assert
        assertNull(invitationService.getAcceptedInvitationEmailFromSession())
    }

    @Test
    fun `getAcceptedInvitationEmailFromSession returns the saved email`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ACCEPTED_ORGANISATIONAL_LANDLORD_INVITATION_EMAIL)).thenReturn("invitee@example.com")

        // Act & Assert
        assertEquals("invitee@example.com", invitationService.getAcceptedInvitationEmailFromSession())
    }

    @Test
    fun `addJourneyIdInvitationTokenPairToSession adds the journey id and invitation token to the session`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(null)

        // Act
        invitationService.addJourneyIdInvitationTokenPairToSession(journeyId, token.toString())

        // Assert
        verify(mockHttpSession)
            .setAttribute(
                ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS,
                mutableListOf(journeyId to token.toString()),
            )
    }

    @Test
    fun `addJourneyIdInvitationTokenPairToSession preserves existing pairs in the session`() {
        // Arrange
        val existingPair = "existing-journey-id" to "existing-token"
        val existingPairs = mutableListOf(existingPair)
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(existingPairs)

        // Act
        invitationService.addJourneyIdInvitationTokenPairToSession(journeyId, token.toString())

        // Assert
        verify(mockHttpSession)
            .setAttribute(
                ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS,
                mutableListOf(existingPair, journeyId to token.toString()),
            )
    }

    @Test
    fun `getInvitationForJourneyIdOrNull returns the invitation for a token associated with the journey`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to token.toString()))
        val invitation = mock<OrganisationalLandlordInvitation>()
        whenever(mockInvitationRepository.findByToken(token)).thenReturn(invitation)

        // Act
        val result = invitationService.getInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertEquals(invitation, result)
    }

    @Test
    fun `getInvitationForJourneyIdOrNull returns null when the journey has no stored token`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(null)

        // Act
        val result = invitationService.getInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertNull(result)
        verifyNoInteractions(mockInvitationRepository)
    }

    @Test
    fun `getInvitationForJourneyIdOrNull returns null when the stored token is malformed`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to "not-a-uuid"))

        // Act
        val result = invitationService.getInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertNull(result)
        verifyNoInteractions(mockInvitationRepository)
    }

    @Test
    fun `getInvitationForJourneyIdOrNull returns null when no invitation matches the stored token`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to token.toString()))
        whenever(mockInvitationRepository.findByToken(token)).thenReturn(null)

        // Act
        val result = invitationService.getInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertNull(result)
    }

    @Test
    fun `getPendingInvitationForJourneyIdOrNull returns the invitation when it is pending`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to token.toString()))
        val organisationalLandlord = createOrgLandlord(name = "Test Organisation")
        val invitation =
            MockLandlordData.createOrganisationalLandlordInvitation(organisationalLandlord = organisationalLandlord)
        whenever(mockInvitationRepository.findByToken(token)).thenReturn(invitation)

        // Act
        val result = invitationService.getPendingInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertEquals(invitation, result)
    }

    @Test
    fun `getPendingInvitationForJourneyIdOrNull returns null when the invitation has expired`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to token.toString()))
        val expiredInvitation =
            MockLandlordData.createOrganisationalLandlordInvitation(
                createdDate =
                    Instant.now().minus(
                        (ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(),
                        ChronoUnit.DAYS,
                    ),
            )
        whenever(mockInvitationRepository.findByToken(token)).thenReturn(expiredInvitation)

        // Act
        val result = invitationService.getPendingInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertNull(result)
    }

    @Test
    fun `getPendingInvitationForJourneyIdOrNull returns null when the invitation is hidden`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to token.toString()))
        val hiddenInvitation = MockLandlordData.createOrganisationalLandlordInvitation(isHidden = true)
        whenever(mockInvitationRepository.findByToken(token)).thenReturn(hiddenInvitation)

        // Act
        val result = invitationService.getPendingInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertNull(result)
    }

    @Test
    fun `getPendingInvitationForJourneyIdOrNull returns null when no invitation matches the stored token`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to token.toString()))
        whenever(mockInvitationRepository.findByToken(token)).thenReturn(null)

        // Act
        val result = invitationService.getPendingInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertNull(result)
    }

    @Test
    fun `getPendingInvitationForJourneyIdOrNull returns null when the journey has no stored token`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(null)

        // Act
        val result = invitationService.getPendingInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertNull(result)
        verifyNoInteractions(mockInvitationRepository)
    }

    @Test
    fun `getPendingInvitationForJourneyIdOrNull returns null when the stored token is malformed`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to "not-a-uuid"))

        // Act
        val result = invitationService.getPendingInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertNull(result)
        verifyNoInteractions(mockInvitationRepository)
    }

    @Test
    fun `createInvitation saves an invitation for the organisation with the given email, role and a token`() {
        // Arrange
        val organisation = createOrgLandlord()
        whenever(mockAbsoluteUrlProvider.buildOrganisationalLandlordInvitationUri(any()))
            .thenReturn(URI("https://www.example.com/landlord/accept-invitation"))

        // Act
        invitationService.createInvitation("invitee@example.com", EDITOR, organisation)

        // Assert
        val invitationCaptor = argumentCaptor<OrganisationalLandlordInvitation>()
        verify(mockInvitationRepository).save(invitationCaptor.capture())
        val savedInvitation = invitationCaptor.firstValue
        assertEquals("invitee@example.com", savedInvitation.invitedEmail)
        assertEquals(EDITOR, savedInvitation.role)
        assertEquals(organisation, savedInvitation.organisationalLandlord)
        assertNotNull(savedInvitation.token)
    }

    @Test
    fun `createInvitation emails the invitee a link to accept the saved invitation`() {
        // Arrange
        val organisation = createOrgLandlord(name = "Example Housing Association")
        val invitationUri = URI("https://www.example.com/landlord/accept-invitation?token=abc")
        whenever(mockAbsoluteUrlProvider.buildOrganisationalLandlordInvitationUri(any())).thenReturn(invitationUri)

        // Act
        invitationService.createInvitation("invitee@example.com", EDITOR, organisation)

        // Assert
        val invitationCaptor = argumentCaptor<OrganisationalLandlordInvitation>()
        verify(mockInvitationRepository).save(invitationCaptor.capture())
        verify(mockAbsoluteUrlProvider).buildOrganisationalLandlordInvitationUri(invitationCaptor.firstValue.token.toString())
        verify(mockInvitationEmailSender).sendEmail(
            "invitee@example.com",
            OrganisationalLandlordInvitationEmail(
                organisationName = "Example Housing Association",
                invitationUri = invitationUri,
            ),
        )
    }

    @Test
    fun `createInvitation propagates the exception when the invitation email fails to send`() {
        // Arrange
        val organisation = createOrgLandlord()
        whenever(mockAbsoluteUrlProvider.buildOrganisationalLandlordInvitationUri(any()))
            .thenReturn(URI("https://www.example.com/landlord/accept-invitation"))
        doThrow(TransientEmailSentException("Email failed to send"))
            .whenever(mockInvitationEmailSender)
            .sendEmail(any(), any())

        // Act & Assert
        assertThrows<TransientEmailSentException> {
            invitationService.createInvitation("invitee@example.com", EDITOR, organisation)
        }
    }

    @Test
    fun `createInvitation does not save an invitation when the email address has already been invited`() {
        // Arrange
        val organisation = createOrgLandlord()
        whenever(mockInvitationRepository.findByOrganisationalLandlord(organisation)).thenReturn(
            listOf(
                MockLandlordData.createOrganisationalLandlordInvitation(
                    invitedEmail = "invitee@example.com",
                    organisationalLandlord = organisation,
                    role = EDITOR,
                ),
            ),
        )

        // Act
        invitationService.createInvitation("Invitee@Example.com", EDITOR, organisation)

        // Assert
        verify(mockInvitationRepository, never()).save(any())
    }

    @Test
    fun `createInvitation saves an invitation when the email address's previous invitation has expired`() {
        // Arrange
        val organisation = createOrgLandlord()
        whenever(mockInvitationRepository.findByOrganisationalLandlord(organisation)).thenReturn(
            listOf(
                MockLandlordData.createOrganisationalLandlordInvitation(
                    invitedEmail = "invitee@example.com",
                    organisationalLandlord = organisation,
                    role = EDITOR,
                    createdDate =
                        Instant.now().minus(
                            (ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(),
                            ChronoUnit.DAYS,
                        ),
                ),
            ),
        )
        whenever(mockAbsoluteUrlProvider.buildOrganisationalLandlordInvitationUri(any()))
            .thenReturn(URI("https://www.example.com/landlord/accept-invitation"))

        // Act
        invitationService.createInvitation("invitee@example.com", EDITOR, organisation)

        // Assert
        verify(mockInvitationRepository).save(any())
    }

    @Test
    fun `createInvitation does not save an invitation when the email address belongs to a team member`() {
        // Arrange
        val organisation = createOrgLandlord()
        whenever(mockOrganisationalLandlordUserService.getOrganisationalLandlordUsers(organisation)).thenReturn(
            listOf(OrganisationalLandlordUser(organisation, PrsdbUser("user-123"), "Team Member", "invitee@example.com", EDITOR)),
        )

        // Act
        invitationService.createInvitation("invitee@example.com", EDITOR, organisation)

        // Assert
        verify(mockInvitationRepository, never()).save(any())
    }

    @Test
    fun `getPendingInvitedAndMemberEmails returns the organisation's invited and team member email addresses`() {
        // Arrange
        val organisation = createOrgLandlord()
        whenever(mockInvitationRepository.findByOrganisationalLandlord(organisation)).thenReturn(
            listOf(
                MockLandlordData.createOrganisationalLandlordInvitation(
                    invitedEmail = "first@example.com",
                    organisationalLandlord = organisation,
                    role = EDITOR,
                ),
                MockLandlordData.createOrganisationalLandlordInvitation(
                    invitedEmail = "second@example.com",
                    organisationalLandlord = organisation,
                    role = EDITOR,
                ),
            ),
        )
        whenever(mockOrganisationalLandlordUserService.getOrganisationalLandlordUsers(organisation)).thenReturn(
            listOf(OrganisationalLandlordUser(organisation, PrsdbUser("user-123"), "Team Member", "member@example.com", EDITOR)),
        )

        // Act
        val emails = invitationService.getPendingInvitedAndMemberEmails(organisation)

        // Assert
        assertEquals(listOf("first@example.com", "second@example.com", "member@example.com"), emails)
    }

    @Test
    fun `getPendingInvitedAndMemberEmails excludes expired and hidden invitations`() {
        // Arrange
        val organisation = createOrgLandlord()
        whenever(mockInvitationRepository.findByOrganisationalLandlord(organisation)).thenReturn(
            listOf(
                MockLandlordData.createOrganisationalLandlordInvitation(
                    invitedEmail = "pending@example.com",
                    organisationalLandlord = organisation,
                    role = EDITOR,
                ),
                MockLandlordData.createOrganisationalLandlordInvitation(
                    invitedEmail = "expired@example.com",
                    organisationalLandlord = organisation,
                    role = EDITOR,
                    createdDate =
                        Instant.now().minus(
                            (ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(),
                            ChronoUnit.DAYS,
                        ),
                ),
                MockLandlordData.createOrganisationalLandlordInvitation(
                    invitedEmail = "hidden@example.com",
                    organisationalLandlord = organisation,
                    role = EDITOR,
                    isHidden = true,
                ),
            ),
        )
        whenever(mockOrganisationalLandlordUserService.getOrganisationalLandlordUsers(organisation)).thenReturn(emptyList())

        // Act
        val emails = invitationService.getPendingInvitedAndMemberEmails(organisation)

        // Assert
        assertEquals(listOf("pending@example.com"), emails)
    }

    @Test
    fun `getInvitationsForOrganisation returns the organisation's invitations from the repository`() {
        // Arrange
        val organisation = createOrgLandlord()
        val invitations = listOf(createOrganisationalLandlordInvitation(organisationalLandlord = organisation))
        whenever(mockInvitationRepository.findByOrganisationalLandlord(organisation)).thenReturn(invitations)

        // Act
        val result = invitationService.getInvitationsForOrganisation(organisation)

        // Assert
        assertEquals(invitations, result)
    }

    @Test
    fun `clearJourneyIdInvitationTokenPairsForTokenFromSession removes only pairs for the accepted token`() {
        // Arrange
        val unrelatedPair = "unrelated-journey-id" to "unrelated-token"
        val sessionPairs =
            mutableListOf(
                journeyId to token.toString(),
                "another-journey-id" to token.toString(),
                unrelatedPair,
            )
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(sessionPairs)

        // Act
        invitationService.clearJourneyIdInvitationTokenPairsForTokenFromSession(token.toString())

        // Assert
        verify(mockHttpSession)
            .setAttribute(
                ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS,
                mutableListOf(unrelatedPair),
            )
    }

    @Test
    fun `acceptInvitation throws when there is no pending invitation for the journey`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf<Pair<String, String>>())

        // Act, Assert
        assertThrows<PrsdbWebException> {
            invitationService.acceptInvitation(journeyId, acceptingUserId, inviteeName, inviteeEmail)
        }
        verifyNoInteractions(mockPrsdbUserService, mockOrganisationalLandlordUserService)
        verify(mockInvitationRepository, never()).delete(any())
    }

    @Test
    fun `acceptInvitation finds or creates a PRSDB user for the accepting user`() {
        // Arrange
        arrangePendingInvitationAcceptance(EDITOR)

        // Act
        invitationService.acceptInvitation(journeyId, acceptingUserId, inviteeName, inviteeEmail)

        // Assert
        verify(mockPrsdbUserService).findOrCreatePrsdbUser(acceptingUserId)
    }

    @ParameterizedTest
    @CsvSource(
        "EDITOR, editor, false",
        "ADMIN, administrator, true",
    )
    fun `acceptInvitation sends confirmation with the invited role to the accepted invitee`(
        role: OrganisationalLandlordUserRole,
        expectedRoleName: String,
        expectedIsAdmin: Boolean,
    ) {
        // Arrange
        arrangePendingInvitationAcceptance(role)

        // Act
        invitationService.acceptInvitation(journeyId, acceptingUserId, inviteeName, inviteeEmail)

        // Assert
        verify(mockAcceptedEmailSender).sendEmail(
            inviteeEmail,
            OrganisationalLandlordInvitationAcceptedEmail(
                recipientName = inviteeName,
                roleName = expectedRoleName,
                organisationName = "Example Organisation",
                isAdmin = expectedIsAdmin,
                dashboardUrl = URI("https://www.example.com/landlord/dashboard"),
            ),
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `acceptInvitation succeeds when the confirmation email fails to send`(isPersistentFailure: Boolean) {
        // Arrange
        val mocks = arrangePendingInvitationAcceptance(EDITOR)
        val exception =
            if (isPersistentFailure) {
                PersistentEmailSendException("Email failed to send")
            } else {
                TransientEmailSentException("Email failed to send")
            }
        doThrow(exception)
            .whenever(mockAcceptedEmailSender)
            .sendEmail(any(), any())

        // Act
        val acceptedToken = invitationService.acceptInvitation(journeyId, acceptingUserId, inviteeName, inviteeEmail)

        // Assert
        assertEquals(token.toString(), acceptedToken)
        verify(mockOrganisationalLandlordUserService)
            .createOrganisationalLandlordUser(
                mocks.invitedLandlord,
                mocks.baseUser,
                inviteeName,
                inviteeEmail,
                EDITOR,
            )
        verify(mockInvitationRepository).delete(mocks.invitation)
        verify(mockAcceptedEmailSender).sendEmail(any(), any())
    }

    @Test
    fun `acceptInvitation sends the confirmation email only after the transaction commits`() {
        // Arrange
        arrangePendingInvitationAcceptance(ADMIN)
        TransactionSynchronizationManager.initSynchronization()
        try {
            // Act
            invitationService.acceptInvitation(journeyId, acceptingUserId, inviteeName, inviteeEmail)

            // Assert
            verifyNoInteractions(mockAcceptedEmailSender)
            TransactionSynchronizationManager.getSynchronizations().forEach { it.afterCommit() }
            verify(mockAcceptedEmailSender).sendEmail(any(), any())
        } finally {
            TransactionSynchronizationManager.clearSynchronization()
        }
    }

    @Test
    fun `acceptInvitation saves a user using the invitation organisation and role`() {
        // Arrange
        val mocks = arrangePendingInvitationAcceptance(EDITOR)

        // Act
        invitationService.acceptInvitation(journeyId, acceptingUserId, inviteeName, inviteeEmail)

        // Assert
        verify(mockOrganisationalLandlordUserService)
            .createOrganisationalLandlordUser(
                mocks.invitedLandlord,
                mocks.baseUser,
                inviteeName,
                inviteeEmail,
                EDITOR,
            )
    }

    @Test
    fun `acceptInvitation deletes the invitation`() {
        // Arrange
        val mocks = arrangePendingInvitationAcceptance(EDITOR)

        // Act
        invitationService.acceptInvitation(journeyId, acceptingUserId, inviteeName, inviteeEmail)

        // Assert
        verify(mockInvitationRepository).delete(mocks.invitation)
    }

    @Test
    fun `acceptInvitation returns the accepted invitation's token`() {
        // Arrange
        arrangePendingInvitationAcceptance(EDITOR)

        // Act
        val acceptedToken = invitationService.acceptInvitation(journeyId, acceptingUserId, inviteeName, inviteeEmail)

        // Assert
        assertEquals(token.toString(), acceptedToken)
    }

    @Test
    fun `acceptInvitation does not delete the invitation when saving the user fails`() {
        // Arrange
        val mocks =
            arrangePendingInvitationAcceptance(
                role = ADMIN,
                stubConfirmationEmailDetails = false,
            )
        whenever(
            mockOrganisationalLandlordUserService.createOrganisationalLandlordUser(
                mocks.invitedLandlord,
                mocks.baseUser,
                inviteeName,
                inviteeEmail,
                ADMIN,
            ),
        ).thenThrow(IllegalStateException("User save failed"))

        // Act, Assert
        assertThrows(IllegalStateException::class.java) {
            invitationService.acceptInvitation(journeyId, acceptingUserId, inviteeName, inviteeEmail)
        }
        verify(mockInvitationRepository, never()).delete(mocks.invitation)
        verify(mockAcceptedEmailSender, never()).sendEmail(any(), any())
    }

    @Test
    fun `hideExpiredInvitation hides and saves an expired invitation`() {
        // Arrange
        val organisation = createOrgLandlord()
        val invitation =
            createOrganisationalLandlordInvitation(
                id = 1,
                organisationalLandlord = organisation,
                createdDate = Instant.now().minus(60, ChronoUnit.DAYS),
            )
        whenever(mockInvitationRepository.findById(1)).thenReturn(Optional.of(invitation))

        // Act
        invitationService.hideExpiredInvitation(1, organisation)

        // Assert
        assertTrue(invitation.isHidden)
        assertEquals(OrganisationalLandlordInvitationStatus.HIDDEN, invitation.status)
        verify(mockInvitationRepository).save(invitation)
    }

    @Test
    fun `hideExpiredInvitation throws 404 when the invitation does not exist`() {
        // Arrange
        whenever(mockInvitationRepository.findById(1)).thenReturn(Optional.empty())

        // Act, Assert
        val exception =
            assertThrows<ResponseStatusException> {
                invitationService.hideExpiredInvitation(1, createOrgLandlord())
            }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `hideExpiredInvitation throws 403 when the invitation belongs to another organisation`() {
        // Arrange
        val otherOrganisation = createOrgLandlord(name = "Other organisation")
        val invitation =
            createOrganisationalLandlordInvitation(
                id = 1,
                organisationalLandlord = otherOrganisation,
                createdDate = Instant.now().minus(60, ChronoUnit.DAYS),
            )
        whenever(mockInvitationRepository.findById(1)).thenReturn(Optional.of(invitation))

        // Act, Assert
        val exception =
            assertThrows<ResponseStatusException> {
                invitationService.hideExpiredInvitation(1, createOrgLandlord())
            }
        assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
        verify(mockInvitationRepository, never()).save(any())
    }

    @Test
    fun `hideExpiredInvitation throws 400 when the invitation is still pending`() {
        // Arrange
        val organisation = createOrgLandlord()
        val invitation =
            createOrganisationalLandlordInvitation(id = 1, organisationalLandlord = organisation)
        whenever(mockInvitationRepository.findById(1)).thenReturn(Optional.of(invitation))

        // Act, Assert
        val exception =
            assertThrows<ResponseStatusException> {
                invitationService.hideExpiredInvitation(1, organisation)
            }
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        verify(mockInvitationRepository, never()).save(any())
    }

    @Test
    fun `hideExpiredInvitation throws 400 when the invitation is already hidden`() {
        // Arrange
        val organisation = createOrgLandlord()
        val invitation =
            createOrganisationalLandlordInvitation(
                id = 1,
                organisationalLandlord = organisation,
                createdDate = Instant.now().minus(60, ChronoUnit.DAYS),
                isHidden = true,
            )
        whenever(mockInvitationRepository.findById(1)).thenReturn(Optional.of(invitation))

        // Act, Assert
        val exception =
            assertThrows<ResponseStatusException> {
                invitationService.hideExpiredInvitation(1, organisation)
            }
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        verify(mockInvitationRepository, never()).save(any())
    }

    private fun arrangePendingInvitationAcceptance(
        role: OrganisationalLandlordUserRole,
        stubConfirmationEmailDetails: Boolean = true,
    ): AcceptInvitationMocks {
        val baseUser = PrsdbUser(acceptingUserId)
        val invitedLandlord = mock<OrganisationalLandlord>()
        val invitation = mock<OrganisationalLandlordInvitation>()
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to token.toString()))
        whenever(mockInvitationRepository.findByToken(token)).thenReturn(invitation)
        whenever(invitation.organisationalLandlord).thenReturn(invitedLandlord)
        whenever(invitation.role).thenReturn(role)
        whenever(invitation.status).thenReturn(OrganisationalLandlordInvitationStatus.PENDING)
        whenever(mockPrsdbUserService.findOrCreatePrsdbUser(acceptingUserId)).thenReturn(baseUser)
        if (stubConfirmationEmailDetails) {
            val dashboardUrl = URI("https://www.example.com/landlord/dashboard")
            whenever(invitedLandlord.name).thenReturn("Example Organisation")
            whenever(invitation.token).thenReturn(token)
            whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUrl)
        }
        whenever(
            mockOrganisationalLandlordUserService.createOrganisationalLandlordUser(
                invitedLandlord,
                baseUser,
                inviteeName,
                inviteeEmail,
                role,
            ),
        ).thenReturn(mock())

        return AcceptInvitationMocks(invitation, invitedLandlord, baseUser)
    }

    private data class AcceptInvitationMocks(
        val invitation: OrganisationalLandlordInvitation,
        val invitedLandlord: OrganisationalLandlord,
        val baseUser: PrsdbUser,
    )
}
