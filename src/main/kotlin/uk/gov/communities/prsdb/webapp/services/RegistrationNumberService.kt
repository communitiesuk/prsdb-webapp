package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.MAX_REGISTRATION_NUMBER
import uk.gov.communities.prsdb.webapp.constants.MIN_REGISTRATION_NUMBER
import uk.gov.communities.prsdb.webapp.constants.MAX_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.MIN_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.REG_NUM_BASE
import uk.gov.communities.prsdb.webapp.constants.REG_NUM_CHARSET
import uk.gov.communities.prsdb.webapp.database.entity.RegisteredEntity
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.RegistrationNumberRepository
import uk.gov.communities.prsdb.webapp.enums.RegistrationNumberType

@Service
class RegistrationNumberService(
    val regNumRepository: RegistrationNumberRepository,
    val landlordRepository: LandlordRepository,
) {
    fun createRegistrationNumber(type: RegistrationNumberType) {
        regNumRepository.save(RegistrationNumber(type, generateUniqueRegNum()))
    }

    fun retrieveEntity(formattedRegNum: String): RegisteredEntity? {
        val charsetRegNum = formattedRegNum.substring(2, 6) + formattedRegNum.substring(7)
        val decRegNum = charsetToDec(charsetRegNum)

        val regNumType = RegistrationNumberType.initialToType(formattedRegNum[0])
        return when (regNumType) {
            RegistrationNumberType.LANDLORD -> landlordRepository.findByRegistrationNumberNumber(decRegNum)
            RegistrationNumberType.PROPERTY -> TODO()
            RegistrationNumberType.AGENT -> TODO()
        }
    }

    private fun generateUniqueRegNum(): Long {
        var registrationNumber: Long
        do {
            registrationNumber = (MIN_REGISTRATION_NUMBER..MAX_REGISTRATION_NUMBER).random()
            registrationNumber = (MIN_REG_NUM..MAX_REG_NUM).random()
        } while (regNumRepository.existsByNumber(registrationNumber))
        return registrationNumber
    }

    private fun charsetToDec(charsetRegNum: String): Long {
        var decRegNum = 0L
        for (char in charsetRegNum) {
            decRegNum = REG_NUM_BASE * decRegNum + REG_NUM_CHARSET.indexOf(char)
        }
        return decRegNum
    }
}