package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class OrganisationalLandlordUserServiceTests {
    @Mock
    private lateinit var mockOrganisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @InjectMocks
    private lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    @Mock
    private lateinit var mockOrganisationLandlord: OrganisationalLandlord

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    private fun setAuthenticatedUser(subjectId: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(subjectId, "password", emptyList())
    }

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
    fun `throwIfCurrentUserIsNotAdminOfOrg does not throw when the current user is an admin of the org`() {
        setAuthenticatedUser("user-123")
        whenever(mockOrganisationLandlord.id).thenReturn(1L)
        val orgUser = MockLandlordData.createOrganisationalLandlordUser(role = OrganisationalLandlordUserRole.ADMIN)
        whenever(mockOrganisationalLandlordUserRepository.findByBaseUser_IdAndOrganisationalLandlord_Id("user-123", 1L))
            .thenReturn(orgUser)

        assertDoesNotThrow { organisationalLandlordUserService.throwIfCurrentUserIsNotAdminOfOrg(mockOrganisationLandlord) }
    }

    @Test
    fun `throwIfCurrentUserIsNotAdminOfOrg throws 403 when the current user is an editor of the org`() {
        setAuthenticatedUser("user-123")
        whenever(mockOrganisationLandlord.id).thenReturn(1L)
        val orgUser = MockLandlordData.createOrganisationalLandlordUser(role = OrganisationalLandlordUserRole.EDITOR)
        whenever(mockOrganisationalLandlordUserRepository.findByBaseUser_IdAndOrganisationalLandlord_Id("user-123", 1L))
            .thenReturn(orgUser)

        val exception =
            assertThrows<ResponseStatusException> {
                organisationalLandlordUserService.throwIfCurrentUserIsNotAdminOfOrg(mockOrganisationLandlord)
            }
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
    }

    @Test
    fun `throwIfCurrentUserIsNotAdminOfOrg throws 403 when the current user is not a member of the org`() {
        setAuthenticatedUser("user-123")
        whenever(mockOrganisationLandlord.id).thenReturn(1L)
        whenever(mockOrganisationalLandlordUserRepository.findByBaseUser_IdAndOrganisationalLandlord_Id("user-123", 1L))
            .thenReturn(null)

        val exception =
            assertThrows<ResponseStatusException> {
                organisationalLandlordUserService.throwIfCurrentUserIsNotAdminOfOrg(mockOrganisationLandlord)
            }
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
    }
}
