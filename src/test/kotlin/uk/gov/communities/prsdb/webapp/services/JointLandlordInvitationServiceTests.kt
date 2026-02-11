package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.TokenNotFoundException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.util.Optional
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class JointLandlordInvitationServiceTests {
    private lateinit var mockJointLandlordInvitationRepository: JointLandlordInvitationRepository
    private lateinit var invitationService: JointLandlordInvitationService
    private lateinit var session: HttpSession

    @BeforeEach
    fun setup() {
        mockJointLandlordInvitationRepository = mock()
        session = MockHttpSession()
        invitationService = JointLandlordInvitationService(mockJointLandlordInvitationRepository, session)
    }

    @Test
    fun `createInvitationToken saves the created invite token for the given property ownership`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        val token = invitationService.createInvitationToken("email@example.com", propertyOwnership)

        val inviteCaptor = captor<JointLandlordInvitation>()
        verify(mockJointLandlordInvitationRepository).save(inviteCaptor.capture())

        assertEquals(propertyOwnership, inviteCaptor.value.registeredPropertyId)
        assertEquals(token, inviteCaptor.value.token.toString())
        assertEquals("email@example.com", inviteCaptor.value.invitedEmail)
    }

    @Test
    fun `getPropertyOwnershipForToken returns the property ownership the token was created with`() {
        val testUuid = UUID.randomUUID()
        val testPropertyOwnership = MockLandlordData.createPropertyOwnership(id = 789)

        whenever(mockJointLandlordInvitationRepository.findByToken(testUuid))
            .thenReturn(MockJointLandlordData.createJointLandlordInvitation(propertyOwnership = testPropertyOwnership, token = testUuid))

        val propertyOwnership = invitationService.getPropertyOwnershipForToken(testUuid.toString())

        assertEquals(propertyOwnership, testPropertyOwnership)
    }

    @Test
    fun `getEmailAddressForToken returns the email the invitation was sent to`() {
        val testUuid = UUID.randomUUID()
        val testEmail = "test@example.com"
        whenever(mockJointLandlordInvitationRepository.findByToken(testUuid))
            .thenReturn(MockJointLandlordData.createJointLandlordInvitation(email = testEmail, token = testUuid))

        val email = invitationService.getEmailAddressForToken(testUuid.toString())

        assertEquals(email, testEmail)
    }

    @Test
    fun `getInvitationFromToken returns an invitation if the token is in the database`() {
        val testUuid = UUID.randomUUID()
        val testInvitation = MockJointLandlordData.createJointLandlordInvitation(token = testUuid)
        whenever(mockJointLandlordInvitationRepository.findByToken(testUuid))
            .thenReturn(testInvitation)

        val invitation = invitationService.getInvitationFromToken(testUuid.toString())

        assertEquals(invitation, testInvitation)
    }

    @Test
    fun `getInvitationFromToken throws an exception if the token is not in the database`() {
        val testUuid = UUID.randomUUID()
        whenever(mockJointLandlordInvitationRepository.findByToken(testUuid)).thenReturn(null)

        val thrown = assertThrows(TokenNotFoundException::class.java) { invitationService.getInvitationFromToken(testUuid.toString()) }
        assertEquals("Invitation token not found in database", thrown.message)
    }

    @Test
    fun `getInvitationOrNull returns an invitation if the token is in the database`() {
        val testUuid = UUID.randomUUID()
        val testInvitation = MockJointLandlordData.createJointLandlordInvitation(token = testUuid)
        whenever(mockJointLandlordInvitationRepository.findByToken(testUuid))
            .thenReturn(testInvitation)

        val invitation = invitationService.getInvitationOrNull(testUuid.toString())

        assertEquals(invitation, testInvitation)
    }

    @Test
    fun `getInvitationOrNull returns null if the token is not in the database`() {
        val testUuid = UUID.randomUUID()
        whenever(mockJointLandlordInvitationRepository.findByToken(testUuid)).thenReturn(null)

        val invitation = invitationService.getInvitationOrNull(testUuid.toString())
        assertNull(invitation)
    }

    @Test
    fun `getInvitationHasExpired returns true if the invitation has expired`() {
        val testUuid = UUID.randomUUID()
        val createdDate =
            Clock.System
                .now()
                .minus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS.hours)
                .minus(1.minutes)
                .toJavaInstant()

        val invitation = MockJointLandlordData.createJointLandlordInvitation(token = testUuid, createdDate = createdDate)

        assertTrue(invitationService.getInvitationHasExpired(invitation))
    }

    @Test
    fun `getInvitationHasExpired returns false if the invitation has not expired`() {
        val testUuid = UUID.randomUUID()
        val createdDate =
            Clock.System
                .now()
                .minus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS.hours)
                .plus(30.minutes)
                .toJavaInstant()

        val invitation = MockJointLandlordData.createJointLandlordInvitation(token = testUuid, createdDate = createdDate)

        assertFalse(invitationService.getInvitationHasExpired(invitation))
    }

    @Test
    fun `tokenIsValid returns true if the token is in the database and has not expired`() {
        val testUuid = UUID.randomUUID()
        val createdDate =
            Clock.System
                .now()
                .minus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS.hours)
                .plus(30.minutes)
                .toJavaInstant()
        whenever(mockJointLandlordInvitationRepository.findByToken(testUuid))
            .thenReturn(MockJointLandlordData.createJointLandlordInvitation(token = testUuid, createdDate = createdDate))

        assertTrue(invitationService.tokenIsValid(testUuid.toString()))
    }

    @Test
    fun `tokenIsValid returns false if the token is in the database but has expired`() {
        val testUuid = UUID.randomUUID()
        val createdDate =
            Clock.System
                .now()
                .minus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS.hours)
                .minus(1.minutes)
                .toJavaInstant()

        whenever(mockJointLandlordInvitationRepository.findByToken(testUuid))
            .thenReturn(MockJointLandlordData.createJointLandlordInvitation(token = testUuid, createdDate = createdDate))

        assertFalse(invitationService.tokenIsValid(testUuid.toString()))
    }

    @Test
    fun `tokenIsValid returns false if the token is not in the database`() {
        val testUuid = UUID.randomUUID()
        whenever(mockJointLandlordInvitationRepository.findByToken(testUuid)).thenReturn(null)

        assertFalse(invitationService.tokenIsValid(testUuid.toString()))
    }

    @Test
    fun `getInvitationByIdOrNull returns an invitation if the id is in the database`() {
        val testId = 123.toLong()
        val invitationFromDatabase = MockJointLandlordData.createJointLandlordInvitation(id = testId)

        whenever(mockJointLandlordInvitationRepository.findById(testId))
            .thenReturn(Optional.of(invitationFromDatabase) as Optional<JointLandlordInvitation?>)

        assertEquals(invitationFromDatabase, invitationService.getInvitationByIdOrNull(testId))
    }

    @Test
    fun `getInvitationByIdOrNull returns null if the id is not in the database`() {
        val testId = 123.toLong()

        assertNull(invitationService.getInvitationByIdOrNull(testId))
    }

    @Test
    fun `deleteInvitation by entity deletes the invitation from the repository`() {
        val invitation = MockJointLandlordData.createJointLandlordInvitation()

        invitationService.deleteInvitation(invitation)

        verify(mockJointLandlordInvitationRepository).delete(invitation)
    }

    @Test
    fun `deleteInvitation by id deletes the invitation from the repository`() {
        val invitationId = 123L

        invitationService.deleteInvitation(invitationId)

        verify(mockJointLandlordInvitationRepository).deleteById(invitationId)
    }

    @Test
    fun `throwErrorIfInvitationExists does not throw error if invitation doesn't exist`() {
        val invitation = MockJointLandlordData.createJointLandlordInvitation()
        whenever(mockJointLandlordInvitationRepository.existsById(invitation.id)).thenReturn(false)

        org.junit.jupiter.api.assertDoesNotThrow {
            invitationService.throwErrorIfInvitationExists(invitation)
        }
    }

    @Test
    fun `throwErrorIfInvitationExists throws INTERNAL SERVER ERROR if invitation still exists`() {
        val invitation = MockJointLandlordData.createJointLandlordInvitation()
        whenever(mockJointLandlordInvitationRepository.existsById(invitation.id)).thenReturn(true)

        val errorThrown =
            org.junit.jupiter.api.assertThrows<ResponseStatusException> {
                invitationService.throwErrorIfInvitationExists(invitation)
            }
        kotlin.test.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorThrown.statusCode)
    }

    @Test
    fun `storeTokenInSession stores the token in the session`() {
        val testToken = "test-token-123"

        invitationService.storeTokenInSession(testToken)

        assertEquals(testToken, session.getAttribute("joint-landlord-invitation-token"))
    }

    @Test
    fun `getTokenFromSession returns the token from the session`() {
        val testToken = "test-token-456"
        session.setAttribute("joint-landlord-invitation-token", testToken)

        val token = invitationService.getTokenFromSession()

        assertEquals(testToken, token)
    }

    @Test
    fun `getTokenFromSession returns null if no token in session`() {
        val token = invitationService.getTokenFromSession()

        assertNull(token)
    }

    @Test
    fun `clearTokenFromSession clears the token from the session`() {
        session.setAttribute("joint-landlord-invitation-token", "test-token")

        invitationService.clearTokenFromSession()

        assertNull(session.getAttribute("joint-landlord-invitation-token"))
    }
}
