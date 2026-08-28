package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException

@PrsdbWebService
class LettingAgentPasswordService(
    private val lettingAgentAccessRepository: LettingAgentAccessRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun setPassword(
        lettingAgentAccess: LettingAgentAccess,
        rawPassword: String,
    ) {
        require(rawPassword.isNotBlank()) { "Password must not be blank" }
        if (lettingAgentAccess.encodedPassword != null) {
            throw PrsdbWebException("Password has already been set for letting agent access ${lettingAgentAccess.id}")
        }

        val encoded = passwordEncoder.encode(rawPassword)

        val updatedRows = lettingAgentAccessRepository.setEncodedPasswordIfAbsent(lettingAgentAccess.id, encoded)
        if (updatedRows == 0) {
            throw PrsdbWebException("Password has already been set for letting agent access ${lettingAgentAccess.id}")
        }

        lettingAgentAccess.recordEncodedPassword(encoded)
        lettingAgentAccessRepository.save(lettingAgentAccess)
    }

    fun isPasswordCorrect(
        lettingAgentAccess: LettingAgentAccess,
        rawPassword: String,
    ): Boolean {
        val stored =
            lettingAgentAccess.encodedPassword
                ?: throw PrsdbWebException("No password has been set for letting agent access ${lettingAgentAccess.id}")
        return passwordEncoder.matches(rawPassword, stored)
    }
}
