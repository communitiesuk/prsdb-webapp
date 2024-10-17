package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import kotlin.test.assertNull

class LandlordServiceTests {
    private lateinit var mockLandlordRepository: LandlordRepository
    private lateinit var landlordService: LandlordService

    @BeforeEach
    fun setUp() {
        mockLandlordRepository = mock()
        landlordService = LandlordService(mockLandlordRepository)
    }

    @Test
    fun `retrieveLandlordByRegNum returns a landlord given its registration number`() {
        val regNum = 0L

        landlordService.retrieveLandlordByRegNum(RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, regNum))

        val regNumCaptor = captor<Long>()
        verify(mockLandlordRepository).findByRegistrationNumber_Number(regNumCaptor.capture())
        assertEquals(regNum, regNumCaptor.value)
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
}
