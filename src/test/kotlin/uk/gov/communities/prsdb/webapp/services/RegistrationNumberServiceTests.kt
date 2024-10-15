package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import uk.gov.communities.prsdb.webapp.constants.MAX_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.MIN_REG_NUM
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.enums.RegistrationNumberType

class RegistrationNumberServiceTests : ServiceTest() {
    private lateinit var regNumService: RegistrationNumberService

    @BeforeEach
    fun setup() {
        regNumService = RegistrationNumberService(mockRegNumRepository, mockLandlordRepository)
    }

    @Test
    fun `createRegistrationNumber creates a registration number for the given entity type`() {
        `when`(mockRegNumRepository.existsByNumber(any(Long::class.java))).thenReturn(false)

        regNumService.createRegistrationNumber(RegistrationNumberType.LANDLORD)

        val regNumCaptor = captor<RegistrationNumber>()
        verify(mockRegNumRepository).save(regNumCaptor.capture())
        assertEquals(RegistrationNumberType.LANDLORD, regNumCaptor.value.type)
    }

    @Test
    fun `createRegistrationNumber creates a unique registration number`() {
        `when`(mockRegNumRepository.existsByNumber(any(Long::class.java))).thenReturn(true, false)

        regNumService.createRegistrationNumber(RegistrationNumberType.LANDLORD)

        verify(mockRegNumRepository, times(2)).existsByNumber(any(Long::class.java))
    }

    @Test
    fun `retrieveEntity retrieves a landlord given their registration number`() {
        val decRegNums =
            listOf(
                MIN_REG_NUM,
                MAX_REG_NUM,
            )
        val formattedRegNums = listOf("L-CCCC-CCCC", "L-9999-9999")
        val landlord = Landlord()

        for (i in decRegNums.indices) {
            `when`(mockLandlordRepository.findByRegistrationNumberNumber(decRegNums[i])).thenReturn(landlord)
            assertEquals(regNumService.retrieveEntity(formattedRegNums[i]), landlord)
        }
    }

    @Test
    fun `retrieveEntity returns null given a non-existent registration number`() {
        val decRegNum = 1L
        val formattedRegNum = "L-CCCC-CCCC"
        val landlord = Landlord()

        `when`(mockLandlordRepository.findByRegistrationNumberNumber(decRegNum)).thenReturn(landlord)

        assertNull(regNumService.retrieveEntity(formattedRegNum))
    }
}
