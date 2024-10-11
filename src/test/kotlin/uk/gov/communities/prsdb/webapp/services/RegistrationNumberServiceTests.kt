package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.RegistrationNumberRepository
import uk.gov.communities.prsdb.webapp.enums.RegistrationNumberType

class RegistrationNumberServiceTests {
    private val mockRegNumRepository = mock<RegistrationNumberRepository>()
    private val regNumService = RegistrationNumberService(mockRegNumRepository)

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
}
