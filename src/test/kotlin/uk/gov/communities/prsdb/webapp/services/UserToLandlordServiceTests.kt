package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class UserToLandlordServiceTests {
    @Mock
    private lateinit var individualLandlordRepository: IndividualLandlordRepository

    @Mock
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    private val service: UserToLandlordService
        get() = UserToLandlordService(individualLandlordRepository, organisationalLandlordUserRepository)

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getLandlordForBaseUserId returns individual landlord associated with user`() {
        val baseUserId = "individual-user"
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(landlord)

        val result = service.getLandlordForBaseUserId(baseUserId)

        assertEquals(landlord, result)
    }

    @Test
    fun `getLandlordForBaseUserId returns organisation landlord associated with user`() {
        val baseUserId = "organisation-user"
        val baseUser = MockLandlordData.createPrsdbUser(baseUserId)
        val landlord = OrganisationLandlord()
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)
        whenever(organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(
            listOf(
                OrganisationalLandlordUser(landlord, baseUser, "Alice Registrant", "alice@example.com"),
            ),
        )

        val result = service.getLandlordForBaseUserId(baseUserId)

        assertEquals(landlord, result)
    }

    @Test
    fun `getLandlordForBaseUserId throws when user is not associated with a landlord`() {
        val baseUserId = "non-landlord-user"
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)
        whenever(organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(emptyList())

        assertThrows<ResponseStatusException> {
            service.getLandlordForBaseUserId(baseUserId)
        }
    }

    @Test
    fun `getLandlordForBaseUserIdOrNull returns null when user is not associated with a landlord`() {
        val baseUserId = "non-landlord-user"
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)
        whenever(organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(emptyList())

        val result = service.getLandlordForBaseUserIdOrNull(baseUserId)

        assertNull(result)
    }

    @Test
    fun `getCurrentLandlordForUser returns landlord associated with authenticated user`() {
        val baseUserId = "current-user"
        val landlord = MockLandlordData.createIndividualLandlord()
        setMockPrincipal(baseUserId)
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(landlord)

        val result = service.getCurrentLandlordForUser()

        assertEquals(landlord, result)
        verify(individualLandlordRepository).findByBaseUser_Id(baseUserId)
    }

    @Test
    fun `getCurrentLandlordForUser throws when authenticated user is not associated with a landlord`() {
        val baseUserId = "non-landlord-user"
        setMockPrincipal(baseUserId)
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)
        whenever(organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(emptyList())

        val exception = assertThrows<ResponseStatusException> { service.getCurrentLandlordForUser() }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun `getCurrentOrganisationLandlordForUser returns organisation landlord for authenticated user`() {
        val baseUserId = "organisation-user"
        val baseUser = MockLandlordData.createPrsdbUser(baseUserId)
        val landlord = OrganisationLandlord()
        setMockPrincipal(baseUserId)
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)
        whenever(organisationLandlordUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(
            listOf(
                OrganisationLandlordUser(landlord, baseUser, "Alice Registrant", "alice@example.com"),
            ),
        )

        val result = service.getCurrentOrganisationLandlordForUser()

        assertEquals(landlord, result)
    }

    @Test
    fun `getCurrentOrganisationLandlordForUser throws when authenticated user has an individual landlord`() {
        val baseUserId = "individual-user"
        setMockPrincipal(baseUserId)
        val mockLandlord = MockLandlordData.createIndividualLandlord()
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId))
            .thenReturn(
                mockLandlord,
            )

        val exception =
            assertThrows<IllegalStateException> {
                service.getCurrentOrganisationLandlordForUser()
            }

        assertEquals("Expected organisation landlord, but got INDIVIDUAL", exception.message)
    }

    private fun setMockPrincipal(name: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
