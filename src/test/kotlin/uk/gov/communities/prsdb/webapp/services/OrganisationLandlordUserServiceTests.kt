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
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationLandlordUserRepository
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class OrganisationLandlordUserServiceTests {
    @Mock
    private lateinit var mockOrganisationLandlordUserRepository: OrganisationLandlordUserRepository

    @InjectMocks
    private lateinit var organisationLandlordUserService: OrganisationLandlordUserService

    @Mock
    private lateinit var mockOrganisationLandlord: OrganisationLandlord

    @Test
    fun `createOrganisationLandlordUser saves and returns an OrganisationLandlordUser linking landlord to user`() {
        val baseUser = PrsdbUser("user-123")
        val name = "Alice Registrant"
        val email = "alice@example.com"

        whenever(mockOrganisationLandlordUserRepository.save(any<OrganisationLandlordUser>()))
            .thenAnswer { it.arguments[0] }

        val result =
            organisationLandlordUserService.createOrganisationLandlordUser(
                mockOrganisationLandlord,
                baseUser,
                name,
                email,
            )

        val captor = captor<OrganisationLandlordUser>()
        verify(mockOrganisationLandlordUserRepository).save(captor.capture())

        val saved = captor.value
        assertEquals(mockOrganisationLandlord, saved.organisationLandlord)
        assertEquals(baseUser, saved.baseUser)
        assertEquals(name, saved.name)
        assertEquals(email, saved.email)
        assertEquals(result, saved)
    }
}
