package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class LandlordServiceTests {
    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var mockOneLoginUserRepository: OneLoginUserRepository

    @Mock
    private lateinit var mockAddressService: AddressService

    @Mock
    private lateinit var mockRegistrationNumberService: RegistrationNumberService

    @InjectMocks
    private lateinit var landlordService: LandlordService

    @Test
    fun `retrieveLandlordByRegNum returns a landlord given its registration number`() {
        val regNumDataModel = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 0L)
        val expectedLandlord = Landlord()

        whenever(mockLandlordRepository.findByRegistrationNumber_Number(regNumDataModel.number)).thenReturn(
            expectedLandlord,
        )

        val landlord = landlordService.retrieveLandlordByRegNum(regNumDataModel)

        assertEquals(expectedLandlord, landlord)
    }

    @Test
    fun `retrieveLandlordByRegNum returns a null given a non-existent landlord registration number`() {
        assertNull(
            landlordService.retrieveLandlordByRegNum(
                RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 0L),
            ),
        )
    }

    @Test
    fun `retrieveLandlordByRegNum throws an illegal argument exception when given a non-landlord registration number`() {
        assertThrows<IllegalArgumentException> {
            landlordService.retrieveLandlordByRegNum(
                RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, 0L),
            )
        }
    }

    @Test
    fun `retrieveLandlordByBaseUserId returns a landlord given its base user ID`() {
        val baseUserId = "baseUserId"
        val expectedLandlord = Landlord()

        whenever(mockLandlordRepository.findByBaseUser_Id(baseUserId)).thenReturn(expectedLandlord)

        val landlord = landlordService.retrieveLandlordByBaseUserId(baseUserId)

        assertEquals(expectedLandlord, landlord)
    }

    @Test
    fun `retrieveLandlordByBaseUserId returns a null given an unregistered base user ID`() {
        assertNull(
            landlordService.retrieveLandlordByBaseUserId(
                "unregisteredBaseUserId",
            ),
        )
    }

    @Test
    fun `createLandlord creates a landlord`() {
        val baseUserId = "baseUserId"
        val addressDataModel = AddressDataModel("1 Example Road, EG1 2AB")

        val baseUser = OneLoginUser(baseUserId)
        val address = Address(addressDataModel)
        val registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 1233456)

        val expectedLandlord =
            Landlord(
                baseUser,
                "name",
                "example@email.com",
                "07123456789",
                address,
                registrationNumber,
                null,
                null,
            )

        whenever(mockOneLoginUserRepository.getReferenceById(baseUserId)).thenReturn(baseUser)
        whenever(mockAddressService.findOrCreateAddress(addressDataModel)).thenReturn(address)
        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.LANDLORD)).thenReturn(
            registrationNumber,
        )

        landlordService.createLandlord(
            baseUserId,
            "name",
            "example@email.com",
            "07123456789",
            addressDataModel,
        )

        val landlordCaptor = captor<Landlord>()
        verify(mockLandlordRepository).save(landlordCaptor.capture())
        assertTrue(ReflectionEquals(expectedLandlord, "id").matches(landlordCaptor.value))
    }
}
