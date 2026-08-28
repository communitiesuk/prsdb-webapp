package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData

@ExtendWith(MockitoExtension::class)
class LettingAgentPasswordServiceTests {
    @Mock
    private lateinit var lettingAgentAccessRepository: LettingAgentAccessRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var lettingAgentPasswordService: LettingAgentPasswordService

    @Test
    fun `setPassword rejects a blank password`() {
        val access = MockLettingAgentData.createLettingAgentAccessWithoutPassword()

        assertThrows<IllegalArgumentException> {
            lettingAgentPasswordService.setPassword(access, "   ")
        }

        verify(passwordEncoder, never()).encode(any())
    }

    @Test
    fun `setPassword rejects when password already set on entity`() {
        val access = MockLettingAgentData.createLettingAgentAccess(encodedPassword = "{bcrypt}existing")

        assertThrows<PrsdbWebException> {
            lettingAgentPasswordService.setPassword(access, "newPassword")
        }

        verify(passwordEncoder, never()).encode(any())
    }

    @Test
    fun `setPassword encodes and persists the password`() {
        val access = MockLettingAgentData.createLettingAgentAccessWithoutPassword()
        whenever(passwordEncoder.encode("myPassword")).thenReturn("{bcrypt}encoded")
        whenever(
            lettingAgentAccessRepository.setEncodedPasswordIfAbsent(
                eq(MockLettingAgentData.DEFAULT_LETTING_AGENT_ACCESS_ID),
                eq("{bcrypt}encoded"),
            ),
        ).thenReturn(1)
        whenever(lettingAgentAccessRepository.save(access)).thenReturn(access)

        lettingAgentPasswordService.setPassword(access, "myPassword")

        verify(passwordEncoder).encode("myPassword")
        verify(lettingAgentAccessRepository).setEncodedPasswordIfAbsent(
            MockLettingAgentData.DEFAULT_LETTING_AGENT_ACCESS_ID,
            "{bcrypt}encoded",
        )
        verify(lettingAgentAccessRepository).save(access)
    }

    @Test
    fun `setPassword throws when atomic update changes zero rows`() {
        val access = MockLettingAgentData.createLettingAgentAccessWithoutPassword()
        whenever(passwordEncoder.encode("myPassword")).thenReturn("{bcrypt}encoded")
        whenever(
            lettingAgentAccessRepository.setEncodedPasswordIfAbsent(any(), any()),
        ).thenReturn(0)

        assertThrows<PrsdbWebException> {
            lettingAgentPasswordService.setPassword(access, "myPassword")
        }
    }

    @Test
    fun `isPasswordCorrect returns true for matching password`() {
        val access = MockLettingAgentData.createLettingAgentAccess(encodedPassword = "{bcrypt}stored")
        whenever(passwordEncoder.matches("candidate", "{bcrypt}stored")).thenReturn(true)

        assertTrue(lettingAgentPasswordService.isPasswordCorrect(access, "candidate"))
    }

    @Test
    fun `isPasswordCorrect returns false for non-matching password`() {
        val access = MockLettingAgentData.createLettingAgentAccess(encodedPassword = "{bcrypt}stored")
        whenever(passwordEncoder.matches("wrong", "{bcrypt}stored")).thenReturn(false)

        assertFalse(lettingAgentPasswordService.isPasswordCorrect(access, "wrong"))
    }

    @Test
    fun `isPasswordCorrect throws when no password has been set`() {
        val access = MockLettingAgentData.createLettingAgentAccessWithoutPassword()

        assertThrows<PrsdbWebException> {
            lettingAgentPasswordService.isPasswordCorrect(access, "candidate")
        }
    }
}
