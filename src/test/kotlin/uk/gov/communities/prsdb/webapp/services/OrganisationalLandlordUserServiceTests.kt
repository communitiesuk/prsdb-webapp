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
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class OrganisationalLandlordUserServiceTests {
    @Mock
    private lateinit var mockOrganisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @InjectMocks
    private lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    @Mock
    private lateinit var mockOrganisationLandlord: OrganisationalLandlord

    @Test
    fun `createOrganisationalLandlordUser saves and returns an OrganisationalLandlordUser linking landlord to user`() {
        val baseUser = PrsdbUser("user-123")
        val name = "Alice Registrant"
        val email = "alice@example.com"

        whenever(mockOrganisationalLandlordUserRepository.save(any<OrganisationalLandlordUser>()))
            .thenAnswer { it.arguments[0] }

        val result =
            organisationalLandlordUserService.createOrganisationalLandlordUser(
                mockOrganisationLandlord,
                baseUser,
                name,
                email,
            )

        val captor = captor<OrganisationalLandlordUser>()
        verify(mockOrganisationalLandlordUserRepository).save(captor.capture())

        val saved = captor.value
        assertEquals(mockOrganisationLandlord, saved.organisationalLandlord)
        assertEquals(baseUser, saved.baseUser)
        assertEquals(name, saved.name)
        assertEquals(email, saved.email)
        assertEquals(result, saved)
    }
}
