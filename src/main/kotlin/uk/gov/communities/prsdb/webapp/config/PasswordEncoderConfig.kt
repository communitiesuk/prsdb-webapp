package uk.gov.communities.prsdb.webapp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration

@PrsdbWebConfiguration
class PasswordEncoderConfig(
    @Value("\${argon2.iterations}") private val iterations: Int,
    @Value("\${argon2.memory}") private val memory: Int,
    @Value("\${argon2.parallelism}") private val parallelism: Int,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = Argon2PasswordEncoder(ARGON2_SALT_LENGTH, ARGON2_HASH_LENGTH, parallelism, memory, iterations)

    companion object {
        const val ARGON2_SALT_LENGTH = 16
        const val ARGON2_HASH_LENGTH = 32
    }
}
