package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.RegistrationNumberRepository

class RegistrationNumberServiceTests {
    private lateinit var mockRegNumRepository: RegistrationNumberRepository
    private lateinit var regNumService: RegistrationNumberService

    private val mockRegistrationNumber = RegistrationNumber()

    @BeforeEach
    fun setup() {
        mockRegNumRepository = mock()
        regNumService = RegistrationNumberService(mockRegNumRepository)
    }

    @Test
    fun `createRegistrationNumber creates a registration number for the given entity type`() {
        whenever(mockRegNumRepository.existsByNumber(any(Long::class.java))).thenReturn(false)
        whenever(mockRegNumRepository.save(any(RegistrationNumber::class.java))).thenReturn(mockRegistrationNumber)

        regNumService.createRegistrationNumber(RegistrationNumberType.LANDLORD)

        val regNumCaptor = captor<RegistrationNumber>()
        verify(mockRegNumRepository).save(regNumCaptor.capture())
        assertEquals(RegistrationNumberType.LANDLORD, regNumCaptor.value.type)
    }

    @Test
    fun `createRegistrationNumber creates a unique registration number`() {
        whenever(mockRegNumRepository.existsByNumber(any(Long::class.java))).thenReturn(true, false)
        whenever(mockRegNumRepository.save(any(RegistrationNumber::class.java))).thenReturn(mockRegistrationNumber)

        regNumService.createRegistrationNumber(RegistrationNumberType.LANDLORD)

        verify(mockRegNumRepository, times(2)).existsByNumber(any(Long::class.java))
    }
}
// test
