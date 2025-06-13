package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.WebService
import uk.gov.communities.prsdb.webapp.constants.MAX_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.MIN_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.RegistrationNumberRepository

@WebService
class RegistrationNumberService(
    private val regNumRepository: RegistrationNumberRepository,
) {
    @Transactional
    fun createRegistrationNumber(type: RegistrationNumberType) = regNumRepository.save(RegistrationNumber(type, generateUniqueRegNum()))

    private fun generateUniqueRegNum(): Long {
        var registrationNumber: Long
        do {
            registrationNumber = (MIN_REG_NUM..MAX_REG_NUM).random()
        } while (regNumRepository.existsByNumber(registrationNumber))
        return registrationNumber
    }
}
