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
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_HAD_ACTIVE_PROPERTIES
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PrsdbUserRepository

@ExtendWith(MockitoExtension::class)
class LandlordDeregistrationServiceTests {
    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var mockPrsdbUserRepository: PrsdbUserRepository

    @Mock
    private lateinit var mockUserRolesService: UserRolesService

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @InjectMocks
    private lateinit var landlordDeregistrationService: LandlordDeregistrationService

    @Test
    fun `deregisterLandlord deletes the user from the landlord table, and the prsdb_user table if they are not a different type of user`() {
        val baseUserId = "one-login-user"
        whenever(mockUserRolesService.getAllRolesForSubjectId(baseUserId)).thenReturn(listOf(ROLE_LANDLORD))

        landlordDeregistrationService.deregisterLandlord(baseUserId)

        verify(mockPrsdbUserRepository).deleteById(baseUserId)
    }

    @Test
    fun `deregisterLandlord deletes the user from the landlord table, but not the prsdb_user table if they are a different type of user`() {
        val baseUserId = "one-login-user"
        whenever(mockUserRolesService.getAllRolesForSubjectId(baseUserId)).thenReturn(listOf(ROLE_LANDLORD, ROLE_LOCAL_COUNCIL_USER))

        landlordDeregistrationService.deregisterLandlord(baseUserId)

        verify(mockPrsdbUserRepository, never()).deleteById(baseUserId)
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
