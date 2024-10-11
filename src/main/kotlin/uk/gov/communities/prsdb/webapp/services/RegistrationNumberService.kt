package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.MAX_REGISTRATION_NUMBER
import uk.gov.communities.prsdb.webapp.constants.MIN_REGISTRATION_NUMBER
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.RegistrationNumberRepository
import uk.gov.communities.prsdb.webapp.enums.RegistrationNumberType

@Service
class RegistrationNumberService(
    val regNumRepository: RegistrationNumberRepository,
) {
    fun createRegistrationNumber(type: RegistrationNumberType) {
        regNumRepository.save(RegistrationNumber(type, generateUniqueRegNum()))
    }

    private fun generateUniqueRegNum(): Long {
        var registrationNumber: Long
        do {
            registrationNumber = (MIN_REGISTRATION_NUMBER..MAX_REGISTRATION_NUMBER).random()
        } while (regNumRepository.existsByNumber(registrationNumber))
        return registrationNumber
    }
}
