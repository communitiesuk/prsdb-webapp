package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class UserToOrgLandlordUserServiceTests {
    @Mock
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    private val service: UserToOrgLandlordUserService
        get() = UserToOrgLandlordUserService(organisationalLandlordUserRepository)

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getOrganisationalLandlordUserForCurrentUser returns the record for the authenticated user`() {
        val baseUserId = "organisation-user"
        setMockPrincipal(baseUserId)
        val organisationalLandlordUser = MockLandlordData.createOrganisationalLandlordUser()
        whenever(organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(
            listOf(organisationalLandlordUser),
        )

        val result = service.getOrganisationalLandlordUserForCurrentUser()

        assertEquals(organisationalLandlordUser, result)
    }

    @Test
    fun `getOrganisationalLandlordUserForCurrentUser returns null when the authenticated user is not an org landlord user`() {
        val baseUserId = "non-org-landlord-user"
        setMockPrincipal(baseUserId)
        whenever(organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(emptyList())

        assertNull(service.getOrganisationalLandlordUserForCurrentUser())
    }

    private fun setMockPrincipal(name: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
