package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.SAFE_CHARACTERS_CHARSET
import uk.gov.communities.prsdb.webapp.database.entity.Passcode
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityRepository
import uk.gov.communities.prsdb.webapp.database.repository.PasscodeRepository

@PrsdbWebService
class PasscodeService(
    private val passcodeRepository: PasscodeRepository,
    private val localAuthorityRepository: LocalAuthorityRepository,
) {
    companion object {
        private const val PASSCODE_LENGTH = 6
    }

    @Transactional
    fun generatePasscode(localAuthorityId: Long): Passcode {
        val localAuthority =
            localAuthorityRepository.findById(localAuthorityId.toInt())
                .orElseThrow { IllegalArgumentException("LocalAuthority with id $localAuthorityId not found") }

        var passcodeString: String
        do {
            passcodeString = generateRandomPasscodeString()
        } while (passcodeRepository.existsById(passcodeString))

        val passcode =
            Passcode(
                passcode = passcodeString,
                localAuthority = localAuthority,
            )

        return passcodeRepository.save(passcode)
    }

    private fun generateRandomPasscodeString(): String {
        return (1..PASSCODE_LENGTH)
            .map { SAFE_CHARACTERS_CHARSET[SAFE_CHARACTERS_CHARSET.indices.random()] }
            .joinToString("")
    }
}
