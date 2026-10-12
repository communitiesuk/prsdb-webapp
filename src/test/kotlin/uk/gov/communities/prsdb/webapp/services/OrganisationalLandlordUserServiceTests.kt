package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class OrganisationalLandlordUserServiceTests {
    @Mock
    private lateinit var mockOrganisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @InjectMocks
    private lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    @Mock
    private lateinit var mockOrganisationLandlord: OrganisationalLandlord

    @Mock
    private lateinit var mockOtherOrganisationLandlord: OrganisationalLandlord

    @Mock
    private lateinit var mockUserToLandlordService: UserToLandlordService

    @Test
    fun `createOrganisationalLandlordUser saves and returns an OrganisationalLandlordUser linking landlord to user`() {
        val baseUser = PrsdbUser("user-123")
        val name = "Alice Registrant"
        val email = "alice@example.com"
        val role = OrganisationalLandlordUserRole.EDITOR

        whenever(mockOrganisationalLandlordUserRepository.save(any<OrganisationalLandlordUser>()))
            .thenAnswer { it.arguments[0] }

        val result =
            organisationalLandlordUserService.createOrganisationalLandlordUser(
                mockOrganisationLandlord,
                baseUser,
                name,
                email,
                role,
            )

        val captor = captor<OrganisationalLandlordUser>()
        verify(mockOrganisationalLandlordUserRepository).save(captor.capture())

        val saved = captor.value
        assertEquals(mockOrganisationLandlord, saved.organisationalLandlord)
        assertEquals(baseUser, saved.baseUser)
        assertEquals(name, saved.name)
        assertEquals(email, saved.email)
        assertEquals(role, saved.role)
        assertEquals(result, saved)
    }

    @Test
    fun `getOrganisationalLandlordUserOrNull returns the user when it exists`() {
        val user = MockLandlordData.createOrganisationalLandlordUser()
        whenever(mockOrganisationalLandlordUserRepository.findById(5)).thenReturn(Optional.of(user))

        assertEquals(user, organisationalLandlordUserService.getOrganisationalLandlordUserOrNull(5))
    }

    @Test
    fun `getOrganisationalLandlordUserOrNull returns null when the user does not exist`() {
        whenever(mockOrganisationalLandlordUserRepository.findById(5)).thenReturn(Optional.empty())

        assertNull(organisationalLandlordUserService.getOrganisationalLandlordUserOrNull(5))
    }

    @Test
    fun `updateRole loads the user, sets the new role and saves`() {
        val user = MockLandlordData.createOrganisationalLandlordUser(role = OrganisationalLandlordUserRole.EDITOR)
        whenever(mockOrganisationalLandlordUserRepository.findById(5)).thenReturn(Optional.of(user))

        organisationalLandlordUserService.updateRole(5, OrganisationalLandlordUserRole.ADMIN)

        assertEquals(OrganisationalLandlordUserRole.ADMIN, user.role)
        verify(mockOrganisationalLandlordUserRepository).save(user)
    }

    @Test
    fun `updateRole throws when the user does not exist`() {
        whenever(mockOrganisationalLandlordUserRepository.findById(5)).thenReturn(Optional.empty())

        assertFailsWith<PrsdbWebException> {
            organisationalLandlordUserService.updateRole(5, OrganisationalLandlordUserRole.ADMIN)
        }
    }

    @Test
    fun `getCurrentUserIsAuthorizedToEditRecord returns false when the current user is not an org team member`() {
        whenever(mockUserToLandlordService.getCurrentOrganisationalLandlordTeamMember()).thenReturn(null)

        assertFalse(organisationalLandlordUserService.getCurrentUserIsAuthorizedToEditRecord(5))
    }

    @Test
    fun `getCurrentUserIsAuthorizedToEditRecord returns false when the current user is not an admin of the organisation`() {
        val currentUser =
            MockLandlordData.createOrganisationalLandlordUser(
                organisationalLandlord = mockOrganisationLandlord,
                role = OrganisationalLandlordUserRole.EDITOR,
            )
        whenever(mockUserToLandlordService.getCurrentOrganisationalLandlordTeamMember()).thenReturn(currentUser)

        assertFalse(organisationalLandlordUserService.getCurrentUserIsAuthorizedToEditRecord(5))
    }

    @Test
    fun `getCurrentUserIsAuthorizedToEditRecord returns false when the member does not exist`() {
        val currentUser =
            MockLandlordData.createOrganisationalLandlordUser(
                organisationalLandlord = mockOrganisationLandlord,
                role = OrganisationalLandlordUserRole.ADMIN,
            )
        whenever(mockUserToLandlordService.getCurrentOrganisationalLandlordTeamMember()).thenReturn(currentUser)
        whenever(mockOrganisationalLandlordUserRepository.findById(5)).thenReturn(Optional.empty())

        assertFalse(organisationalLandlordUserService.getCurrentUserIsAuthorizedToEditRecord(5))
    }

    @Test
    fun `getCurrentUserIsAuthorizedToEditRecord returns false when the member belongs to a different organisation`() {
        val currentUser =
            MockLandlordData.createOrganisationalLandlordUser(
                organisationalLandlord = mockOrganisationLandlord,
                role = OrganisationalLandlordUserRole.ADMIN,
            )
        whenever(mockUserToLandlordService.getCurrentOrganisationalLandlordTeamMember()).thenReturn(currentUser)
        whenever(mockOrganisationLandlord.id).thenReturn(1L)
        val member = MockLandlordData.createOrganisationalLandlordUser(organisationalLandlord = mockOtherOrganisationLandlord)
        whenever(mockOtherOrganisationLandlord.id).thenReturn(2L)
        whenever(mockOrganisationalLandlordUserRepository.findById(5)).thenReturn(Optional.of(member))

        assertFalse(organisationalLandlordUserService.getCurrentUserIsAuthorizedToEditRecord(5))
    }

    @Test
    fun `getCurrentUserIsAuthorizedToEditRecord returns true when the member belongs to the current user's organisation`() {
        val currentUser =
            MockLandlordData.createOrganisationalLandlordUser(
                organisationalLandlord = mockOrganisationLandlord,
                role = OrganisationalLandlordUserRole.ADMIN,
            )
        whenever(mockUserToLandlordService.getCurrentOrganisationalLandlordTeamMember()).thenReturn(currentUser)
        whenever(mockOrganisationLandlord.id).thenReturn(1L)
        val member = MockLandlordData.createOrganisationalLandlordUser(organisationalLandlord = mockOrganisationLandlord)
        whenever(mockOrganisationalLandlordUserRepository.findById(5)).thenReturn(Optional.of(member))

        assertTrue(organisationalLandlordUserService.getCurrentUserIsAuthorizedToEditRecord(5))
    }
}
