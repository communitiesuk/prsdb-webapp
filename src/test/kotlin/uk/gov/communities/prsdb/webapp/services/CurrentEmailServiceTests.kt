package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class CurrentEmailServiceTests {
    @Mock
    private lateinit var individualLandlordRepository: IndividualLandlordRepository

    @Mock
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    private val service: CurrentEmailService
        get() = CurrentEmailService(individualLandlordRepository, organisationalLandlordUserRepository)

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getCurrentEmail and getCurrentName return the authenticated individual landlord details`() {
        // Arrange
        val baseUserId = "individual-user"
        val landlord =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                name = "Individual User",
                email = "individual@example.com",
            )
        setMockPrincipal(baseUserId)
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(landlord)
        whenever(organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(emptyList())

        // Act, Assert
        assertEquals("individual@example.com", service.getCurrentEmail())
        assertEquals("Individual User", service.getCurrentName())

        verify(individualLandlordRepository, times(2)).findByBaseUser_Id(baseUserId)
        verify(organisationalLandlordUserRepository, times(2)).findByBaseUser_Id(baseUserId)
    }

    @ParameterizedTest
    @EnumSource(OrganisationalLandlordUserRole::class)
    fun `getCurrentEmail and getCurrentName return the authenticated organisational user details`(role: OrganisationalLandlordUserRole) {
        // Arrange
        val baseUserId = "organisation-user"
        val organisationalLandlord = MockLandlordData.createOrgLandlord()
        val organisationalUser =
            MockLandlordData.createOrganisationalLandlordUser(
                organisationalLandlord = organisationalLandlord,
                baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                name = "Organisation User",
                email = "organisation-user@example.com",
                role = role,
            )
        setMockPrincipal(baseUserId)
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)
        whenever(organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId))
            .thenReturn(listOf(organisationalUser))

        // Act, Assert
        assertEquals("organisation-user@example.com", service.getCurrentEmail())
        assertEquals("Organisation User", service.getCurrentName())
    }

    @Test
    fun `getCurrentEmail throws bad request when the authenticated user has no landlord record`() {
        // Arrange
        val baseUserId = "missing-user"
        setMockPrincipal(baseUserId)
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)
        whenever(organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(emptyList())

        // Act
        val exception = assertThrows<ResponseStatusException> { service.getCurrentEmail() }

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        assertEquals("No landlord was found for user with baseUserId $baseUserId", exception.reason)
    }

    @Test
    fun `getCurrentName throws when the authenticated user belongs to multiple organisations`() {
        // Arrange
        val baseUserId = "multi-organisation-user"
        setMockPrincipal(baseUserId)
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)
        whenever(organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId))
            .thenReturn(
                listOf(
                    MockLandlordData.createOrganisationalLandlordUser(
                        organisationalLandlord = MockLandlordData.createOrgLandlord(),
                        baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                    ),
                    MockLandlordData.createOrganisationalLandlordUser(
                        organisationalLandlord = MockLandlordData.createOrgLandlord(),
                        baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                    ),
                ),
            )

        // Act
        val exception = assertThrows<PrsdbWebException> { service.getCurrentName() }

        // Assert
        assertEquals("Multiple landlords were found for user with baseUserId $baseUserId", exception.message)
    }

    @Test
    fun `getCurrentEmail throws when the authenticated user has individual and organisation records`() {
        // Arrange
        val baseUserId = "ambiguous-user"
        setMockPrincipal(baseUserId)
        whenever(individualLandlordRepository.findByBaseUser_Id(baseUserId))
            .thenReturn(
                MockLandlordData.createIndividualLandlord(
                    baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                ),
            )
        whenever(organisationalLandlordUserRepository.findByBaseUser_Id(baseUserId))
            .thenReturn(
                listOf(
                    MockLandlordData.createOrganisationalLandlordUser(
                        organisationalLandlord = MockLandlordData.createOrgLandlord(),
                        baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                    ),
                ),
            )

        // Act, Assert
        assertThrows<PrsdbWebException> { service.getCurrentEmail() }
    }

    private fun setMockPrincipal(name: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
