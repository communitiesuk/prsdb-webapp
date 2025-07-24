package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.SAFE_CHARACTERS_CHARSET
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.Passcode
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityRepository
import uk.gov.communities.prsdb.webapp.database.repository.PasscodeRepository
import java.util.*

class PasscodeServiceTests {
    private lateinit var mockPasscodeRepository: PasscodeRepository
    private lateinit var mockLocalAuthorityRepository: LocalAuthorityRepository
    private lateinit var passcodeService: PasscodeService

    private val mockLocalAuthority = LocalAuthority()
    private val mockPasscode = Passcode()

    @BeforeEach
    fun setup() {
        mockPasscodeRepository = mock()
        mockLocalAuthorityRepository = mock()
        passcodeService = PasscodeService(mockPasscodeRepository, mockLocalAuthorityRepository)
    }

    @Test
    fun `generatePasscode creates a valid passcode for the given local authority`() {
        val localAuthorityId = 123L
        whenever(mockLocalAuthorityRepository.findById(anyInt())).thenReturn(Optional.of(mockLocalAuthority))
        whenever(mockPasscodeRepository.existsById(anyString())).thenReturn(false)
        whenever(mockPasscodeRepository.save(any(Passcode::class.java))).thenReturn(mockPasscode)

        val result = passcodeService.generatePasscode(localAuthorityId)

        assertEquals(mockPasscode, result)
        verify(mockLocalAuthorityRepository).findById(localAuthorityId.toInt())

        val passcodeCaptor = captor<Passcode>()
        verify(mockPasscodeRepository).save(passcodeCaptor.capture())
        val savedPasscode = passcodeCaptor.value

        // Verify passcode properties
        assertEquals(6, savedPasscode.passcode.length)
        assertTrue(
            savedPasscode.passcode.all { char -> SAFE_CHARACTERS_CHARSET.contains(char) },
            "Generated passcode '${savedPasscode.passcode}' contains unsafe characters"
        )
        assertNotNull(savedPasscode.passcode)
        assertEquals(mockLocalAuthority, savedPasscode.localAuthority)
    }

    @Test
    fun `generatePasscode handles collisions and creates unique passcode`() {
        val localAuthorityId = 123L
        whenever(mockLocalAuthorityRepository.findById(anyInt())).thenReturn(Optional.of(mockLocalAuthority))
        whenever(mockPasscodeRepository.existsById(anyString())).thenReturn(true, true, false)
        whenever(mockPasscodeRepository.save(any(Passcode::class.java))).thenReturn(mockPasscode)

        passcodeService.generatePasscode(localAuthorityId)

        verify(mockPasscodeRepository, times(3)).existsById(anyString())
        verify(mockPasscodeRepository, times(1)).save(any(Passcode::class.java))
    }

    @Test
    fun `generatePasscode throws exception when local authority not found`() {
        val localAuthorityId = 999L
        whenever(mockLocalAuthorityRepository.findById(anyInt())).thenReturn(Optional.empty())

        val exception = assertThrows(IllegalArgumentException::class.java) {
            passcodeService.generatePasscode(localAuthorityId)
        }

        assertEquals("LocalAuthority with id $localAuthorityId not found", exception.message)
    }
}
