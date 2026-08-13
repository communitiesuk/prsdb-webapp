package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_HAD_ACTIVE_PROPERTIES
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationGoverningBodyMemberRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PrsdbUserRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class LandlordDeregistrationServiceTests {
    @Mock
    private lateinit var mockIndividualLandlordRepository: IndividualLandlordRepository

    @Mock
    private lateinit var mockOrganisationLandlordRepository: OrganisationLandlordRepository

    @Mock
    private lateinit var mockOrganisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @Mock
    private lateinit var mockOrganisationGoverningBodyMemberRepository: OrganisationGoverningBodyMemberRepository

    @Mock
    private lateinit var mockPropertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPrsdbUserRepository: PrsdbUserRepository

    @Mock
    private lateinit var mockUserRolesService: UserRolesService

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @InjectMocks
    private lateinit var landlordDeregistrationService: LandlordDeregistrationService

    @Test
    fun `deregisterIndividualLandlord deletes the user from the landlord table, prsdb_user table if not a different type of user`() {
        val baseUserId = "one-login-user"
        whenever(mockUserRolesService.getAllRolesForSubjectId(baseUserId)).thenReturn(listOf(ROLE_LANDLORD))

        landlordDeregistrationService.deregisterIndividualLandlord(baseUserId)

        verify(mockPrsdbUserRepository).deleteById(baseUserId)
    }

    @Test
    fun `deregisterIndividualLandlord deletes the user from the landlord table, not prsdb_user table if a different type of user`() {
        val baseUserId = "one-login-user"
        whenever(mockUserRolesService.getAllRolesForSubjectId(baseUserId)).thenReturn(listOf(ROLE_LANDLORD, ROLE_LOCAL_COUNCIL_USER))

        landlordDeregistrationService.deregisterIndividualLandlord(baseUserId)

        verify(mockPrsdbUserRepository, never()).deleteById(baseUserId)
    }

    @Test
    fun `deregisterIndividualLandlord deletes properties the landlord solely owns`() {
        val baseUserId = "one-login-user"
        val landlord = MockLandlordData.createIndividualLandlord()
        ReflectionTestUtils.setField(landlord, "id", 1L)
        val soleProperty = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord), id = 10L)

        whenever(mockIndividualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(landlord)
        whenever(mockUserRolesService.getAllRolesForSubjectId(baseUserId)).thenReturn(listOf(ROLE_LANDLORD))

        landlordDeregistrationService.deregisterIndividualLandlord(baseUserId)

        verify(mockPropertyOwnershipRepository).deleteAll(listOf(soleProperty))
    }

    @Test
    fun `deregisterIndividualLandlord does not delete properties with another co-owner`() {
        val baseUserId = "one-login-user"
        val landlord = MockLandlordData.createIndividualLandlord()
        ReflectionTestUtils.setField(landlord, "id", 1L)
        val coLandlord = MockLandlordData.createIndividualLandlord()
        ReflectionTestUtils.setField(coLandlord, "id", 2L)
        val jointProperty = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord), id = 20L)
        jointProperty.addLandlord(coLandlord)

        whenever(mockIndividualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(landlord)
        whenever(mockUserRolesService.getAllRolesForSubjectId(baseUserId)).thenReturn(listOf(ROLE_LANDLORD))

        landlordDeregistrationService.deregisterIndividualLandlord(baseUserId)

        verify(mockPropertyOwnershipRepository).deleteAll(emptyList())
        verify(mockPropertyOwnershipService).removeLandlord(jointProperty, landlord)
    }

    @Test
    fun `deregisterIndividualLandlord deletes only solely-owned properties when the landlord owns a mix`() {
        val baseUserId = "one-login-user"
        val landlord = MockLandlordData.createIndividualLandlord()
        ReflectionTestUtils.setField(landlord, "id", 1L)
        val coLandlord = MockLandlordData.createIndividualLandlord()
        ReflectionTestUtils.setField(coLandlord, "id", 2L)
        val soleProperty = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord), id = 10L)
        val jointProperty = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(landlord, coLandlord), id = 20L)

        whenever(mockIndividualLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(landlord)
        whenever(mockUserRolesService.getAllRolesForSubjectId(baseUserId)).thenReturn(listOf(ROLE_LANDLORD))

        landlordDeregistrationService.deregisterIndividualLandlord(baseUserId)

        verify(mockPropertyOwnershipRepository).deleteAll(listOf(soleProperty))
        verify(mockPropertyOwnershipService).removeLandlord(jointProperty, landlord)
    }

    @Test
    fun `deregisterOrganisationalLandlord deletes solely owned properties`() {
        val orgLandlord = MockLandlordData.createOrgLandlord()
        ReflectionTestUtils.setField(orgLandlord, "id", 1L)
        val soleProperty = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(orgLandlord), id = 10L)
        whenever(mockOrganisationalLandlordUserRepository.findByOrganisationalLandlord(orgLandlord)).thenReturn(emptyList())

        landlordDeregistrationService.deregisterOrganisationalLandlord(orgLandlord)

        verify(mockPropertyOwnershipRepository).deleteAll(listOf(soleProperty))
    }

    @Test
    fun `deregisterOrganisationalLandlord removes landlord from jointly owned properties`() {
        val orgLandlord = MockLandlordData.createOrgLandlord()
        ReflectionTestUtils.setField(orgLandlord, "id", 1L)
        val coLandlord = MockLandlordData.createIndividualLandlord()
        ReflectionTestUtils.setField(coLandlord, "id", 2L)
        val jointProperty = MockLandlordData.createPropertyOwnership(landlords = mutableSetOf(orgLandlord), id = 20L)
        jointProperty.addLandlord(coLandlord)
        whenever(mockOrganisationalLandlordUserRepository.findByOrganisationalLandlord(orgLandlord)).thenReturn(emptyList())

        landlordDeregistrationService.deregisterOrganisationalLandlord(orgLandlord)

        verify(mockPropertyOwnershipRepository).deleteAll(emptyList())
        verify(mockPropertyOwnershipService).removeLandlord(jointProperty, orgLandlord)
    }

    @Test
    fun `deregisterOrganisationalLandlord deletes governing body members`() {
        val orgLandlord = MockLandlordData.createOrgLandlord()
        whenever(mockOrganisationalLandlordUserRepository.findByOrganisationalLandlord(orgLandlord)).thenReturn(emptyList())

        landlordDeregistrationService.deregisterOrganisationalLandlord(orgLandlord)

        verify(mockOrganisationGoverningBodyMemberRepository).deleteByOrganisationalLandlord(orgLandlord)
    }

    @Test
    fun `deregisterOrganisationalLandlord deletes organisational landlord users`() {
        val orgLandlord = MockLandlordData.createOrgLandlord()
        val baseUser = MockLandlordData.createPrsdbUser()
        val olu = OrganisationalLandlordUser(orgLandlord, baseUser, "User", "user@example.com")
        whenever(mockOrganisationalLandlordUserRepository.findByOrganisationalLandlord(orgLandlord)).thenReturn(listOf(olu))
        whenever(mockUserRolesService.getAllRolesForSubjectId(baseUser.id)).thenReturn(listOf(ROLE_LANDLORD))

        landlordDeregistrationService.deregisterOrganisationalLandlord(orgLandlord)

        verify(mockOrganisationalLandlordUserRepository).deleteAll(listOf(olu))
    }

    @Test
    fun `deregisterOrganisationalLandlord deletes the org landlord record`() {
        val orgLandlord = MockLandlordData.createOrgLandlord()
        whenever(mockOrganisationalLandlordUserRepository.findByOrganisationalLandlord(orgLandlord)).thenReturn(emptyList())

        landlordDeregistrationService.deregisterOrganisationalLandlord(orgLandlord)

        verify(mockOrganisationLandlordRepository).delete(orgLandlord)
    }

    @Test
    fun `deregisterOrganisationalLandlord deletes base user if they have no other roles`() {
        val orgLandlord = MockLandlordData.createOrgLandlord()
        val baseUser = MockLandlordData.createPrsdbUser()
        val olu = OrganisationalLandlordUser(orgLandlord, baseUser, "User", "user@example.com")
        whenever(mockOrganisationalLandlordUserRepository.findByOrganisationalLandlord(orgLandlord)).thenReturn(listOf(olu))
        whenever(mockUserRolesService.getAllRolesForSubjectId(baseUser.id)).thenReturn(listOf(ROLE_LANDLORD))

        landlordDeregistrationService.deregisterOrganisationalLandlord(orgLandlord)

        verify(mockPrsdbUserRepository).deleteById(baseUser.id)
    }

    @Test
    fun `deregisterOrganisationalLandlord does not delete base user if they have other roles`() {
        val orgLandlord = MockLandlordData.createOrgLandlord()
        val baseUser = MockLandlordData.createPrsdbUser()
        val olu = OrganisationalLandlordUser(orgLandlord, baseUser, "User", "user@example.com")
        whenever(mockOrganisationalLandlordUserRepository.findByOrganisationalLandlord(orgLandlord)).thenReturn(listOf(olu))
        whenever(mockUserRolesService.getAllRolesForSubjectId(baseUser.id)).thenReturn(listOf(ROLE_LANDLORD, ROLE_LOCAL_COUNCIL_USER))

        landlordDeregistrationService.deregisterOrganisationalLandlord(orgLandlord)

        verify(mockPrsdbUserRepository, never()).deleteById(baseUser.id)
    }

    @Test
    fun `addLandlordHadActivePropertiesToSession adds a boolean attribute to the session`() {
        landlordDeregistrationService.addLandlordHadActivePropertiesToSession(true)

        verify(mockHttpSession).setAttribute(LANDLORD_HAD_ACTIVE_PROPERTIES, true)
    }

    @Test
    fun `getLandlordHadActivePropertiesFromSession gets a boolean from the session`() {
        whenever(mockHttpSession.getAttribute(LANDLORD_HAD_ACTIVE_PROPERTIES)).thenReturn(true)

        assertTrue(landlordDeregistrationService.getLandlordHadActivePropertiesFromSession())
    }
}
