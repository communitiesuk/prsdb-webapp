package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class LandlordUserEmailServiceTests {
    @Mock
    private lateinit var individualLandlordRepository: IndividualLandlordRepository

    @Mock
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @InjectMocks
    private lateinit var landlordUserEmailService: LandlordUserEmailService

    @Test
    fun `getEmailsByBaseUserId returns the individual landlord's email keyed by base user id`() {
        val user = MockLandlordData.createPrsdbUser("individual-user")
        val landlord = MockLandlordData.createIndividualLandlord(baseUser = user, email = "individual@example.com")

        whenever(individualLandlordRepository.findByBaseUser_IdIn(listOf(user.id))).thenReturn(listOf(landlord))
        whenever(organisationalLandlordUserRepository.findByBaseUser_IdIn(listOf(user.id))).thenReturn(emptyList())

        val result = landlordUserEmailService.getEmailsByBaseUserId(listOf(user.id))

        assertEquals(mapOf(user.id to "individual@example.com"), result)
    }

    @Test
    fun `getEmailsByBaseUserId returns each organisation user's own email rather than the organisation's`() {
        val user1 = MockLandlordData.createPrsdbUser("org-user-1")
        val user2 = MockLandlordData.createPrsdbUser("org-user-2")
        val organisation = MockLandlordData.createOrgLandlord()
        val orgUser1 = createOrganisationalLandlordUser(organisation, user1, "org.user.one@example.com")
        val orgUser2 = createOrganisationalLandlordUser(organisation, user2, "org.user.two@example.com")

        whenever(individualLandlordRepository.findByBaseUser_IdIn(listOf(user1.id, user2.id))).thenReturn(emptyList())
        whenever(organisationalLandlordUserRepository.findByBaseUser_IdIn(listOf(user1.id, user2.id)))
            .thenReturn(listOf(orgUser1, orgUser2))

        val result = landlordUserEmailService.getEmailsByBaseUserId(listOf(user1.id, user2.id))

        assertEquals(
            mapOf(user1.id to "org.user.one@example.com", user2.id to "org.user.two@example.com"),
            result,
        )
    }

    @Test
    fun `getEmailsByBaseUserId resolves a mixed batch of users with one query per repository`() {
        val individualUser = MockLandlordData.createPrsdbUser("individual-user")
        val organisationUser = MockLandlordData.createPrsdbUser("org-user")
        val requestedIds = listOf(individualUser.id, organisationUser.id)
        val landlord = MockLandlordData.createIndividualLandlord(baseUser = individualUser, email = "individual@example.com")
        val orgUser =
            createOrganisationalLandlordUser(MockLandlordData.createOrgLandlord(), organisationUser, "org.user@example.com")

        whenever(individualLandlordRepository.findByBaseUser_IdIn(requestedIds)).thenReturn(listOf(landlord))
        whenever(organisationalLandlordUserRepository.findByBaseUser_IdIn(requestedIds)).thenReturn(listOf(orgUser))

        val result = landlordUserEmailService.getEmailsByBaseUserId(requestedIds)

        assertEquals(
            mapOf(individualUser.id to "individual@example.com", organisationUser.id to "org.user@example.com"),
            result,
        )
        verify(individualLandlordRepository, times(1)).findByBaseUser_IdIn(any())
        verify(organisationalLandlordUserRepository, times(1)).findByBaseUser_IdIn(any())
    }

    @Test
    fun `getEmailsByBaseUserId omits users that have no landlord`() {
        val userWithLandlord = MockLandlordData.createPrsdbUser("individual-user")
        val userWithoutLandlord = MockLandlordData.createPrsdbUser("user-without-landlord")
        val requestedIds = listOf(userWithLandlord.id, userWithoutLandlord.id)
        val landlord = MockLandlordData.createIndividualLandlord(baseUser = userWithLandlord, email = "individual@example.com")

        whenever(individualLandlordRepository.findByBaseUser_IdIn(requestedIds)).thenReturn(listOf(landlord))
        whenever(organisationalLandlordUserRepository.findByBaseUser_IdIn(requestedIds)).thenReturn(emptyList())

        val result = landlordUserEmailService.getEmailsByBaseUserId(requestedIds)

        assertEquals(mapOf(userWithLandlord.id to "individual@example.com"), result)
    }

    @Test
    fun `getEmailsByBaseUserId returns an empty map without querying when given no ids`() {
        val result = landlordUserEmailService.getEmailsByBaseUserId(emptyList())

        assertEquals(emptyMap(), result)
        verify(individualLandlordRepository, never()).findByBaseUser_IdIn(any())
        verify(organisationalLandlordUserRepository, never()).findByBaseUser_IdIn(any())
    }

    private fun createOrganisationalLandlordUser(
        organisationalLandlord: OrganisationalLandlord,
        baseUser: PrsdbUser,
        email: String,
    ) = OrganisationalLandlordUser(
        organisationalLandlord = organisationalLandlord,
        baseUser = baseUser,
        name = "Organisation user",
        email = email,
    )
}
