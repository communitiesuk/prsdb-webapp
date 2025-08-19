package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.LAST_GENERATED_PASSCODE
import uk.gov.communities.prsdb.webapp.constants.SAFE_CHARACTERS_CHARSET
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.Passcode
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityRepository
import uk.gov.communities.prsdb.webapp.database.repository.PasscodeRepository
import uk.gov.communities.prsdb.webapp.exceptions.PasscodeLimitExceededException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.util.Optional

class PasscodeServiceTests {
    private lateinit var mockPasscodeRepository: PasscodeRepository
    private lateinit var mockLocalAuthorityRepository: LocalAuthorityRepository
    private lateinit var mockOneLoginUserService: OneLoginUserService
    private lateinit var mockSession: HttpSession
    private lateinit var passcodeService: PasscodeService

    private val mockLocalAuthority = LocalAuthority()
    private val mockPasscode = Passcode()

    @BeforeEach
    fun setup() {
        mockPasscodeRepository = mock()
        mockLocalAuthorityRepository = mock()
        mockOneLoginUserService = mock()
        mockSession = mock()
        passcodeService = PasscodeService(mockPasscodeRepository, mockLocalAuthorityRepository, mockOneLoginUserService, mockSession)
    }

    @Test
    fun `generatePasscode creates a valid passcode for the given local authority`() {
        val localAuthorityId = 123L
        whenever(mockPasscodeRepository.count()).thenReturn(500L)
        whenever(mockLocalAuthorityRepository.findById(anyInt())).thenReturn(Optional.of(mockLocalAuthority))
        whenever(mockPasscodeRepository.existsByPasscode(anyString())).thenReturn(false)
        whenever(mockPasscodeRepository.save(any(Passcode::class.java))).thenReturn(mockPasscode)

        val result = passcodeService.generatePasscode(localAuthorityId)

        assertEquals(mockPasscode, result)
        verify(mockPasscodeRepository).count()
        verify(mockLocalAuthorityRepository).findById(localAuthorityId.toInt())

        val passcodeCaptor = captor<Passcode>()
        verify(mockPasscodeRepository).save(passcodeCaptor.capture())
        val savedPasscode = passcodeCaptor.value

        // Verify passcode properties
        assertEquals(6, savedPasscode.passcode.length)
        assertTrue(
            savedPasscode.passcode.all { char -> SAFE_CHARACTERS_CHARSET.contains(char) },
            "Generated passcode '${savedPasscode.passcode}' contains unsafe characters",
        )
        assertNotNull(savedPasscode.passcode)
        assertEquals(mockLocalAuthority, savedPasscode.localAuthority)
    }

    @Test
    fun `generatePasscode handles collisions and creates unique passcode`() {
        val localAuthorityId = 123L
        whenever(mockPasscodeRepository.count()).thenReturn(500L)
        whenever(mockLocalAuthorityRepository.findById(anyInt())).thenReturn(Optional.of(mockLocalAuthority))
        whenever(mockPasscodeRepository.existsByPasscode(anyString())).thenReturn(true, true, false)
        whenever(mockPasscodeRepository.save(any(Passcode::class.java))).thenReturn(mockPasscode)

        passcodeService.generatePasscode(localAuthorityId)

        verify(mockPasscodeRepository).count()
        verify(mockPasscodeRepository, times(3)).existsByPasscode(anyString())
        verify(mockPasscodeRepository, times(1)).save(any(Passcode::class.java))
    }

    @Test
    fun `generatePasscode throws exception when local authority not found`() {
        val localAuthorityId = 999L
        whenever(mockPasscodeRepository.count()).thenReturn(500L)
        whenever(mockLocalAuthorityRepository.findById(anyInt())).thenReturn(Optional.empty())

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                passcodeService.generatePasscode(localAuthorityId)
            }

        assertEquals("LocalAuthority with id $localAuthorityId not found", exception.message)
        verify(mockPasscodeRepository).count()
    }

    @Test
    fun `generatePasscode throws PasscodeLimitExceededException when limit is reached`() {
        val localAuthorityId = 123L
        whenever(mockPasscodeRepository.count()).thenReturn(1000L)

        val exception =
            assertThrows(PasscodeLimitExceededException::class.java) {
                passcodeService.generatePasscode(localAuthorityId)
            }

        assertEquals("Maximum number of passcodes (1000) has been reached", exception.message)
        verify(mockPasscodeRepository).count()
        verify(mockLocalAuthorityRepository, never()).findById(anyInt())
        verify(mockPasscodeRepository, never()).save(any(Passcode::class.java))
    }

    @Test
    fun `generatePasscode throws PasscodeLimitExceededException when limit is exceeded`() {
        val localAuthorityId = 123L
        whenever(mockPasscodeRepository.count()).thenReturn(1001L)

        val exception =
            assertThrows(PasscodeLimitExceededException::class.java) {
                passcodeService.generatePasscode(localAuthorityId)
            }

        assertEquals("Maximum number of passcodes (1000) has been reached", exception.message)
        verify(mockPasscodeRepository).count()
        verify(mockLocalAuthorityRepository, never()).findById(anyInt())
        verify(mockPasscodeRepository, never()).save(any(Passcode::class.java))
    }

    @Test
    fun `generatePasscode succeeds when count is just below limit`() {
        val localAuthorityId = 123L
        whenever(mockPasscodeRepository.count()).thenReturn(999L)
        whenever(mockLocalAuthorityRepository.findById(anyInt())).thenReturn(Optional.of(mockLocalAuthority))
        whenever(mockPasscodeRepository.existsByPasscode(anyString())).thenReturn(false)
        whenever(mockPasscodeRepository.save(any(Passcode::class.java))).thenReturn(mockPasscode)

        val result = passcodeService.generatePasscode(localAuthorityId)

        assertEquals(mockPasscode, result)
        verify(mockPasscodeRepository).count()
        verify(mockLocalAuthorityRepository).findById(localAuthorityId.toInt())
        verify(mockPasscodeRepository).save(any(Passcode::class.java))
    }

    @Test
    fun `generateAndStorePasscode creates and stores a valid passcode for the given local authority`() {
        val localAuthorityId = 123L
        val mockPasscodeValue = "ABC123"
        val mockPasscodeWithValue = Passcode(mockPasscodeValue, mockLocalAuthority)
        whenever(mockPasscodeRepository.count()).thenReturn(500L)
        whenever(mockLocalAuthorityRepository.findById(anyInt())).thenReturn(Optional.of(mockLocalAuthority))
        whenever(mockPasscodeRepository.existsByPasscode(anyString())).thenReturn(false)
        whenever(mockPasscodeRepository.save(any(Passcode::class.java))).thenReturn(mockPasscodeWithValue)

        val result = passcodeService.generateAndStorePasscode(localAuthorityId)

        assertEquals(mockPasscodeValue, result)
        verify(mockPasscodeRepository).count()
        verify(mockLocalAuthorityRepository).findById(localAuthorityId.toInt())

        val passcodeCaptor = captor<Passcode>()
        verify(mockPasscodeRepository).save(passcodeCaptor.capture())
        val savedPasscode = passcodeCaptor.value

        // Verify passcode properties
        assertEquals(6, savedPasscode.passcode.length)
        assertTrue(
            savedPasscode.passcode.all { char -> SAFE_CHARACTERS_CHARSET.contains(char) },
            "Generated passcode '${savedPasscode.passcode}' contains unsafe characters",
        )
        assertNotNull(savedPasscode.passcode)
        assertEquals(mockLocalAuthority, savedPasscode.localAuthority)
    }

    @Test
    fun `generateAndStorePasscode throws PasscodeLimitExceededException when limit is reached`() {
        val localAuthorityId = 123L
        whenever(mockPasscodeRepository.count()).thenReturn(1000L)

        val exception =
            assertThrows(PasscodeLimitExceededException::class.java) {
                passcodeService.generateAndStorePasscode(localAuthorityId)
            }

        assertEquals("Maximum number of passcodes (1000) has been reached", exception.message)
        verify(mockPasscodeRepository).count()
        verify(mockPasscodeRepository, times(0)).save(any(Passcode::class.java))
    }

    @Test
    fun `getOrGeneratePasscode throws PasscodeLimitExceededException when limit is reached and no cached passcode exists`() {
        val localAuthorityId = 123L
        whenever(mockSession.getAttribute(LAST_GENERATED_PASSCODE)).thenReturn(null)
        whenever(mockPasscodeRepository.count()).thenReturn(1000L)

        val exception =
            assertThrows(PasscodeLimitExceededException::class.java) {
                passcodeService.getOrGeneratePasscode(localAuthorityId)
            }

        assertEquals("Maximum number of passcodes (1000) has been reached", exception.message)
        verify(mockPasscodeRepository).count()
    }

    @Test
    fun `getOrGeneratePasscode returns cached passcode when limit is reached but passcode exists in session`() {
        val localAuthorityId = 123L
        val cachedPasscode = "ABC123"
        whenever(mockSession.getAttribute(LAST_GENERATED_PASSCODE)).thenReturn(cachedPasscode)
        whenever(mockPasscodeRepository.count()).thenReturn(1000L)

        val result = passcodeService.getOrGeneratePasscode(localAuthorityId)

        assertEquals(cachedPasscode, result)
        verify(mockSession).getAttribute(LAST_GENERATED_PASSCODE)
        verify(mockPasscodeRepository, never()).count()
        verify(mockLocalAuthorityRepository, never()).findById(anyInt())
        verify(mockPasscodeRepository, never()).save(any(Passcode::class.java))
    }

    @Test
    fun `getOrGeneratePasscode generates new passcode when no cached passcode exists`() {
        val localAuthorityId = 123L
        val mockPasscodeValue = "XYZ789"
        val mockPasscodeWithValue = Passcode(mockPasscodeValue, mockLocalAuthority)
        whenever(mockSession.getAttribute(LAST_GENERATED_PASSCODE)).thenReturn(null)
        whenever(mockPasscodeRepository.count()).thenReturn(500L)
        whenever(mockLocalAuthorityRepository.findById(anyInt())).thenReturn(Optional.of(mockLocalAuthority))
        whenever(mockPasscodeRepository.existsByPasscode(anyString())).thenReturn(false)
        whenever(mockPasscodeRepository.save(any(Passcode::class.java))).thenReturn(mockPasscodeWithValue)

        val result = passcodeService.getOrGeneratePasscode(localAuthorityId)

        assertEquals(mockPasscodeValue, result)
        verify(mockSession).getAttribute(LAST_GENERATED_PASSCODE)
        verify(mockSession).setAttribute(LAST_GENERATED_PASSCODE, mockPasscodeValue)
        verify(mockPasscodeRepository).count()
        verify(mockLocalAuthorityRepository).findById(localAuthorityId.toInt())
        verify(mockPasscodeRepository).save(any(Passcode::class.java))
    }

    @Test
    fun `isValidPasscode returns true for existing passcode`() {
        val passcode = "ABC123"
        whenever(mockPasscodeRepository.existsByPasscode(passcode)).thenReturn(true)

        val result = passcodeService.isValidPasscode(passcode)

        assertTrue(result)
        verify(mockPasscodeRepository).existsByPasscode(passcode)
    }

    @Test
    fun `isValidPasscode returns false for non-existing passcode`() {
        val passcode = "INVALID"
        whenever(mockPasscodeRepository.existsByPasscode(passcode)).thenReturn(false)

        val result = passcodeService.isValidPasscode(passcode)

        assertFalse(result)
        verify(mockPasscodeRepository).existsByPasscode(passcode)
    }

    @Test
    fun `isValidPasscode normalizes passcode by trimming whitespace and converting to uppercase`() {
        val inputPasscode = "  abc123  "
        val normalizedPasscode = "ABC123"
        whenever(mockPasscodeRepository.existsByPasscode(normalizedPasscode)).thenReturn(true)

        val result = passcodeService.isValidPasscode(inputPasscode)

        assertTrue(result)
        verify(mockPasscodeRepository).existsByPasscode(normalizedPasscode)
    }

    @Test
    fun `claimPasscodeForUser returns false if the passcode doesn't exist`() {
        val invalidPasscode = "INVALID"
        whenever(mockPasscodeRepository.findByPasscode(invalidPasscode)).thenReturn(null)
        assertFalse(passcodeService.claimPasscodeForUser(invalidPasscode, "userId"))
    }

    @Test
    fun `claimPasscodeForUser returns false if the passcode is already claimed`() {
        val claimedPasscode = MockLandlordData.createPasscode(code = "TAKEN", baseUser = MockLandlordData.createOneLoginUser())
        whenever(mockPasscodeRepository.findByPasscode(claimedPasscode.passcode)).thenReturn(claimedPasscode)
        assertFalse(passcodeService.claimPasscodeForUser(claimedPasscode.passcode, "userId"))
    }

    @Test
    fun `claimPasscodeForUser returns true if the method claims the passcode for the user`() {
        val availablePasscode = MockLandlordData.createPasscode(code = "FREE", baseUser = null)
        val user = MockLandlordData.createOneLoginUser(id = "userId")
        whenever(mockPasscodeRepository.findByPasscode(availablePasscode.passcode)).thenReturn(availablePasscode)
        whenever(mockOneLoginUserService.findOrCreate1LUser(user.id)).thenReturn(user)

        val result = passcodeService.claimPasscodeForUser(availablePasscode.passcode, user.id)

        assertTrue(result)
        verify(mockOneLoginUserService).findOrCreate1LUser(user.id)
        assertEquals(user, availablePasscode.baseUser)
    }

    @Test
    fun `isPasscodeClaimedByUser returns false if the passcode does not exist`() {
        val passcode = "NON_EXISTENT"
        whenever(mockPasscodeRepository.findByPasscode(passcode)).thenReturn(null)
        assertFalse(passcodeService.isPasscodeClaimedByUser(passcode, "userId"))
    }

    @Test
    fun `isPasscodeClaimedByUser returns false if the passcode is not claimed by the user`() {
        val passcode = MockLandlordData.createPasscode(code = "TAKEN", baseUser = MockLandlordData.createOneLoginUser(id = "otherUserId"))
        whenever(mockPasscodeRepository.findByPasscode(passcode.passcode)).thenReturn(passcode)
        assertFalse(passcodeService.isPasscodeClaimedByUser(passcode.passcode, "userId"))
    }

    @Test
    fun `isPasscodeClaimedByUser returns true if the passcode is claimed by the user`() {
        val passcode = MockLandlordData.createPasscode(code = "CLAIMED", baseUser = MockLandlordData.createOneLoginUser(id = "userId"))
        whenever(mockPasscodeRepository.findByPasscode(passcode.passcode)).thenReturn(passcode)
        assertTrue(passcodeService.isPasscodeClaimedByUser(passcode.passcode, passcode.baseUser!!.id))
    }
}
