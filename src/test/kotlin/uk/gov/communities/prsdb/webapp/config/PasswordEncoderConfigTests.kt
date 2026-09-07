package uk.gov.communities.prsdb.webapp.config

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasswordEncoderConfigTests {
    private val encoder = PasswordEncoderConfig(iterations = 2, memory = 16384, parallelism = 1).passwordEncoder()

    @Test
    fun `encoded password matches the original raw password`() {
        val raw = "SecureP@ssw0rd"
        val encoded = encoder.encode(raw)
        assertTrue(encoder.matches(raw, encoded))
    }

    @Test
    fun `encoding the same password twice produces different stored values`() {
        val raw = "SecureP@ssw0rd"
        val first = encoder.encode(raw)
        val second = encoder.encode(raw)
        assertNotEquals(first, second)
    }

    @Test
    fun `encoded value contains an argon2 hash prefix`() {
        val encoded = encoder.encode("test")
        assertTrue(encoded.startsWith("\$argon2id"))
    }
}
