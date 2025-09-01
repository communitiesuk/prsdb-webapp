package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_HAD_ACTIVE_PROPERTIES
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class LandlordDeregistrationServiceTests {
    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var mockOneLoginUserRepository: OneLoginUserRepository

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyDeregistrationService: PropertyDeregistrationService

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @Mock
    private lateinit var mockFormContextService: FormContextService

    @Mock
    private lateinit var mockFormContextRepository: FormContextRepository

    @InjectMocks
    private lateinit var landlordDeregistrationService: LandlordDeregistrationService

    @Test
    fun `deregisterLandlordAndTheirProperties deletes the user from the landlord table`() {
        val baseUserId = "one-login-user"
        whenever(
            mockFormContextRepository.findAllByUser_IdAndJourneyType(baseUserId, JourneyType.PROPERTY_REGISTRATION),
        ).thenReturn(emptyList())

        landlordDeregistrationService.deregisterLandlordAndTheirProperties(baseUserId)

        verify(mockFormContextRepository).findAllByUser_IdAndJourneyType(baseUserId, JourneyType.PROPERTY_REGISTRATION)
        verify(mockLandlordRepository).deleteByBaseUser_Id(baseUserId)
    }

    @Test
    fun `deregisterLandlordAndTheirProperties deletes the user from the one-login table if they are not a different type of user`() {
        val baseUserId = "one-login-user"
        whenever(
            mockFormContextRepository.findAllByUser_IdAndJourneyType(baseUserId, JourneyType.PROPERTY_REGISTRATION),
        ).thenReturn(emptyList())

        landlordDeregistrationService.deregisterLandlordAndTheirProperties(baseUserId)

        verify(mockFormContextRepository).findAllByUser_IdAndJourneyType(baseUserId, JourneyType.PROPERTY_REGISTRATION)
        verify(mockOneLoginUserRepository).deleteIfNotLocalAuthorityUser(baseUserId)
    }

    @Test
    fun `deregisterLandlordAndTheirProperties deletes landlord properties and returns the deleted propertyOwnerships`() {
        val landlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createOneLoginUser(id = "one-login-user"))
        val landlordProperties =
            listOf(
                MockLandlordData.createPropertyOwnership(primaryLandlord = landlord),
                MockLandlordData.createPropertyOwnership(primaryLandlord = landlord),
            )
        whenever(mockPropertyOwnershipService.retrieveAllPropertiesForLandlord("one-login-user")).thenReturn(landlordProperties)
        whenever(
            mockFormContextRepository.findAllByUser_IdAndJourneyType("one-login-user", JourneyType.PROPERTY_REGISTRATION),
        ).thenReturn(emptyList())

        val deletedPropertyOwnerships = landlordDeregistrationService.deregisterLandlordAndTheirProperties("one-login-user")

        verify(mockFormContextRepository).findAllByUser_IdAndJourneyType("one-login-user", JourneyType.PROPERTY_REGISTRATION)
        verify(mockPropertyDeregistrationService).deregisterProperties(landlordProperties)
        assertEquals(landlordProperties, deletedPropertyOwnerships)
    }

    @Test
    fun `deregisterLandlordAndTheirProperties does not attempt to delete landlord properties if there are none to delete`() {
        val baseUserId = "one-login-user"
        whenever(
            mockFormContextRepository.findAllByUser_IdAndJourneyType(baseUserId, JourneyType.PROPERTY_REGISTRATION),
        ).thenReturn(emptyList())

        landlordDeregistrationService.deregisterLandlordAndTheirProperties(baseUserId)

        verify(mockFormContextRepository).findAllByUser_IdAndJourneyType(baseUserId, JourneyType.PROPERTY_REGISTRATION)
        verify(mockPropertyDeregistrationService, never()).deregisterProperties(any())
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

    @Test
    fun `deregisterLandlordAndTheirProperties deletes incomplete property registrations if present`() {
        val baseUserId = "one-login-user"
        val mockFormContext = org.mockito.Mockito.mock(FormContext::class.java)
        val incompleteList = listOf(mockFormContext)
        whenever(
            mockFormContextRepository.findAllByUser_IdAndJourneyType(baseUserId, JourneyType.PROPERTY_REGISTRATION),
        ).thenReturn(incompleteList)

        landlordDeregistrationService.deregisterLandlordAndTheirProperties(baseUserId)

        verify(mockFormContextService).deleteFormContexts(incompleteList)
    }
}
