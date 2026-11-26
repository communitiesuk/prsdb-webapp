package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SystemOperatorRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPrsdbUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSystemOperatorData.Companion.createSystemOperator
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ActingAsLandlordServiceTests {
    @Mock
    private lateinit var actingAsLandlordSessionService: ActingAsLandlordSessionService

    @Mock
    private lateinit var landlordService: LandlordService

    @Mock
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @Mock
    private lateinit var systemOperatorRepository: SystemOperatorRepository

    @Mock
    private lateinit var securityContextService: SecurityContextService

    @InjectMocks
    private lateinit var actingAsLandlordService: ActingAsLandlordService

    @Test
    fun `startActingAsLandlord resets the session then stores an individual landlord's user ID`() {
        // Arrange
        val landlord = createIndividualLandlord(baseUser = createPrsdbUser("landlord-user-id"))
        whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)

        // Act
        actingAsLandlordService.startActingAsLandlord(landlord.id, memberId = null)

        // Assert
        val inOrder = inOrder(securityContextService, actingAsLandlordSessionService)
        inOrder.verify(securityContextService).resetSession()
        inOrder.verify(actingAsLandlordSessionService).setActingAsUserId("landlord-user-id")
    }

    @Test
    fun `startActingAsLandlord stores the organisation member's user ID`() {
        // Arrange
        val landlord = createOrgLandlord()
        val member =
            createOrganisationalLandlordUser(
                baseUser = createPrsdbUser("member-user-id"),
                organisationalLandlord = landlord,
            )
        whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)
        whenever(organisationalLandlordUserRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member))

        // Act
        actingAsLandlordService.startActingAsLandlord(landlord.id, MEMBER_ID)

        // Assert
        verify(actingAsLandlordSessionService).setActingAsUserId("member-user-id")
    }

    @Test
    fun `startActingAsLandlord throws NOT_FOUND for an unknown landlord`() {
        assertRejected(HttpStatus.NOT_FOUND) { actingAsLandlordService.startActingAsLandlord(UNKNOWN_LANDLORD_ID, null) }
    }

    @Test
    fun `startActingAsLandlord throws BAD_REQUEST for an individual landlord with a member ID`() {
        // Arrange
        val landlord = createIndividualLandlord()
        whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)

        // Assert
        assertRejected(HttpStatus.BAD_REQUEST) { actingAsLandlordService.startActingAsLandlord(landlord.id, MEMBER_ID) }
    }

    @Test
    fun `startActingAsLandlord throws BAD_REQUEST for an organisation landlord without a member ID`() {
        // Arrange
        val landlord = createOrgLandlord()
        whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)

        // Assert
        assertRejected(HttpStatus.BAD_REQUEST) { actingAsLandlordService.startActingAsLandlord(landlord.id, null) }
    }

    @Test
    fun `startActingAsLandlord throws NOT_FOUND for an unknown member ID`() {
        // Arrange
        val landlord = createOrgLandlord()
        whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)
        whenever(organisationalLandlordUserRepository.findById(MEMBER_ID)).thenReturn(Optional.empty())

        // Assert
        assertRejected(HttpStatus.NOT_FOUND) { actingAsLandlordService.startActingAsLandlord(landlord.id, MEMBER_ID) }
    }

    @Test
    fun `startActingAsLandlord throws NOT_FOUND for a member of a different organisation`() {
        // Arrange
        val landlord = createOrgLandlord()
        val member = createOrganisationalLandlordUser(organisationalLandlord = createOrgLandlord())
        whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)
        whenever(organisationalLandlordUserRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member))

        // Assert
        assertRejected(HttpStatus.NOT_FOUND) { actingAsLandlordService.startActingAsLandlord(landlord.id, MEMBER_ID) }
    }

    @Test
    fun `startActingAsLandlord throws FORBIDDEN when the target user is a system operator`() {
        // Arrange
        val landlord = createIndividualLandlord(baseUser = createPrsdbUser("operator-user-id"))
        whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)
        whenever(systemOperatorRepository.findByBaseUser_Id("operator-user-id")).thenReturn(createSystemOperator())

        // Assert
        assertRejected(HttpStatus.FORBIDDEN) { actingAsLandlordService.startActingAsLandlord(landlord.id, null) }
    }

    @Test
    fun `stopActing resets the session`() {
        // Act
        actingAsLandlordService.stopActing()

        // Assert
        verify(securityContextService).resetSession()
    }

    private fun assertRejected(
        status: HttpStatus,
        action: () -> Unit,
    ) {
        val exception = assertThrows<ResponseStatusException> { action() }
        assertEquals(status, exception.statusCode)
        verify(securityContextService, never()).resetSession()
        verify(actingAsLandlordSessionService, never()).setActingAsUserId(any())
    }

    companion object {
        private const val MEMBER_ID = 7L
        private const val UNKNOWN_LANDLORD_ID = 999L
    }
}
