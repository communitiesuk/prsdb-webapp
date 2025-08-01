package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LAST_GENERATED_PASSCODE
import uk.gov.communities.prsdb.webapp.constants.SAFE_CHARACTERS_CHARSET
import uk.gov.communities.prsdb.webapp.database.entity.Passcode
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityRepository
import uk.gov.communities.prsdb.webapp.database.repository.PasscodeRepository
import uk.gov.communities.prsdb.webapp.exceptions.PasscodeLimitExceededException

@PrsdbWebService
class PasscodeService(
    private val passcodeRepository: PasscodeRepository,
    private val localAuthorityRepository: LocalAuthorityRepository,
    private val session: HttpSession,
) {
    companion object {
        private const val PASSCODE_LENGTH = 6
        private const val MAX_PASSCODES = 1000
    }

    @Transactional
    fun generatePasscode(localAuthorityId: Long): Passcode {
        // Check if passcode limit has been reached
        val currentPasscodeCount = passcodeRepository.count()
        if (currentPasscodeCount >= MAX_PASSCODES) {
            throw PasscodeLimitExceededException("Maximum number of passcodes ($MAX_PASSCODES) has been reached")
        }

        val localAuthority =
            localAuthorityRepository.findById(localAuthorityId.toInt())
                .orElseThrow { IllegalArgumentException("LocalAuthority with id $localAuthorityId not found") }

        var passcodeString: String
        do {
            passcodeString = generateRandomPasscodeString()
        } while (passcodeRepository.existsByPasscode(passcodeString))

        val passcode =
            Passcode(
                passcode = passcodeString,
                localAuthority = localAuthority,
            )

        return passcodeRepository.save(passcode)
    }

    private fun getLastGeneratedPasscode(): String? {
        return session.getAttribute(LAST_GENERATED_PASSCODE) as String?
    }

    private fun setLastGeneratedPasscode(passcode: String) {
        session.setAttribute(LAST_GENERATED_PASSCODE, passcode)
    }

    fun generateAndStorePasscode(localAuthorityId: Long): String {
        val generatedPasscode = generatePasscode(localAuthorityId)
        setLastGeneratedPasscode(generatedPasscode.passcode)
        return generatedPasscode.passcode
    }

    fun getOrGeneratePasscode(localAuthorityId: Long): String {
        return getLastGeneratedPasscode() ?: generateAndStorePasscode(localAuthorityId)
    }

    fun isValidPasscode(passcode: String): Boolean {
        return passcodeRepository.existsByPasscode(passcode)
    }

    private fun generateRandomPasscodeString(): String {
        return (1..PASSCODE_LENGTH)
            .map { SAFE_CHARACTERS_CHARSET.random() }
            .joinToString("")
    }
}
