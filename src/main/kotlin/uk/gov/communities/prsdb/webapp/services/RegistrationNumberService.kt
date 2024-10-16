package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.MAX_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.MIN_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.REG_NUM_BASE
import uk.gov.communities.prsdb.webapp.constants.REG_NUM_CHARSET
import uk.gov.communities.prsdb.webapp.constants.REG_NUM_LENGTH
import uk.gov.communities.prsdb.webapp.constants.REG_NUM_SEG_LENGTH
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegisteredEntity
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.RegistrationNumberRepository

@Service
class RegistrationNumberService(
    val regNumRepository: RegistrationNumberRepository,
    val landlordRepository: LandlordRepository,
) {
    @Transactional
    fun createRegistrationNumber(type: RegistrationNumberType) {
        regNumRepository.save(RegistrationNumber(type, generateUniqueRegNum()))
    }

    fun retrieveEntity(formattedRegNum: String): RegisteredEntity? {
        val charsetRegNum =
            formattedRegNum.substring(2, REG_NUM_SEG_LENGTH + 2) + formattedRegNum.substring(REG_NUM_SEG_LENGTH + 3)
        val decRegNum = charsetToDec(charsetRegNum)

        val regNumType = RegistrationNumberType.initialToType(formattedRegNum[0])
        return when (regNumType) {
            RegistrationNumberType.LANDLORD -> landlordRepository.findByRegistrationNumber_Number(decRegNum)
            RegistrationNumberType.PROPERTY -> TODO()
            RegistrationNumberType.AGENT -> TODO()
        }
    }

    fun formatRegNum(regNum: RegistrationNumber): String {
        var formattedRegNum = ""
        var quotient = regNum.number!!
        while (quotient > 0) {
            formattedRegNum = REG_NUM_CHARSET[(quotient % REG_NUM_BASE).toInt()] + formattedRegNum
            quotient /= REG_NUM_BASE
        }
        formattedRegNum = formattedRegNum.padStart(REG_NUM_LENGTH, REG_NUM_CHARSET[0])

        return regNum.type.toInitial() +
            "-" +
            formattedRegNum.substring(0, REG_NUM_SEG_LENGTH) +
            "-" +
            formattedRegNum.substring(REG_NUM_SEG_LENGTH)
    }

    private fun generateUniqueRegNum(): Long {
        var registrationNumber: Long
        do {
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
