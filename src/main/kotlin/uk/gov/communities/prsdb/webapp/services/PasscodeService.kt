package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.context.annotation.Profile
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LAST_GENERATED_PASSCODE
import uk.gov.communities.prsdb.webapp.constants.SAFE_CHARACTERS_CHARSET
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.Passcode
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.PasscodeRepository
import uk.gov.communities.prsdb.webapp.exceptions.PasscodeLimitExceededException

@PrsdbWebService
@Profile("require-passcode")
class PasscodeService(
    private val passcodeRepository: PasscodeRepository,
    private val localAuthorityRepository: LocalAuthorityRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
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
        val normalizedPasscode = normalizePasscode(passcode)
        return passcodeRepository.existsByPasscode(normalizedPasscode)
    }

    private fun normalizePasscode(passcode: String): String {
        return passcode.trim().uppercase()
    }

    fun hasUserClaimedPasscode(userId: String): Boolean {
        return passcodeRepository.findByBaseUser_Id(userId) != null
    }

    fun findPasscode(passcodeString: String): Passcode? {
        return passcodeRepository.findByPasscode(normalizePasscode(passcodeString))
    }

    @Transactional
    fun claimPasscodeForUser(
        passcodeString: String,
        userId: String,
    ): Boolean {
        val passcode = findPasscode(passcodeString) ?: return false

        if (passcode.baseUser != null) {
            // Already claimed
            return false
        }

        // Find or create the OneLoginUser
        val user =
            oneLoginUserRepository.findById(userId).orElse(null)
                ?: OneLoginUser(userId).also { oneLoginUserRepository.save(it) }

        passcode.claimByUser(user)
        passcodeRepository.save(passcode)
        return true
    }

    fun isPasscodeClaimedByUser(
        passcodeString: String,
        userId: String,
    ): Boolean {
        val passcode = findPasscode(passcodeString) ?: return false
        return passcode.baseUser?.id == userId
    }

    private fun generateRandomPasscodeString(): String {
        return (1..PASSCODE_LENGTH)
            .map { SAFE_CHARACTERS_CHARSET.random() }
            .joinToString("")
    }
}
