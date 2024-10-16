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
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.RegistrationNumberRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

@Service
class RegistrationNumberService(
    val regNumRepository: RegistrationNumberRepository,
) {
    @Transactional
    fun createRegistrationNumber(type: RegistrationNumberType) {
        regNumRepository.save(RegistrationNumber(type, generateUniqueRegNum()))
    }

    fun stringToRegNum(regNumString: String): RegistrationNumberDataModel {
        val baseRegNumString = getBaseRegNumString(regNumString)

        validateBaseRegNumString(baseRegNumString)

        val regNumType = RegistrationNumberType.initialToType(baseRegNumString[0])

        var regNumNumber = 0L
        for (char in baseRegNumString.substring(1)) {
            regNumNumber = REG_NUM_BASE * regNumNumber + REG_NUM_CHARSET.indexOf(char)
        }

        return RegistrationNumberDataModel(regNumType, regNumNumber)
    }

    fun regNumToString(regNum: RegistrationNumber): String {
        var regNumString = ""
        var quotient = regNum.number!!
        while (quotient > 0) {
            regNumString = REG_NUM_CHARSET[(quotient % REG_NUM_BASE).toInt()] + regNumString
            quotient /= REG_NUM_BASE
        }
        regNumString = regNumString.padStart(REG_NUM_LENGTH, REG_NUM_CHARSET[0])

        return regNum.type.toInitial() +
            "-" +
            regNumString.substring(0, REG_NUM_SEG_LENGTH) +
            "-" +
            regNumString.substring(REG_NUM_SEG_LENGTH)
    }

    private fun generateUniqueRegNum(): Long {
        var registrationNumber: Long
        do {
            registrationNumber = (MIN_REG_NUM..MAX_REG_NUM).random()
        } while (regNumRepository.existsByNumber(registrationNumber))
        return registrationNumber
    }

    private fun getBaseRegNumString(regNumString: String): String = regNumString.filter { it.isLetterOrDigit() }.uppercase()

    private fun validateBaseRegNumString(baseRegNumString: String) {
        if (baseRegNumString.length != REG_NUM_LENGTH + 1) {
            throw IllegalArgumentException("Invalid registration number string length")
        }
        if (baseRegNumString.substring(1).any { !REG_NUM_CHARSET.contains(it) }) {
            throw IllegalArgumentException("Invalid registration number string characters")
        }
    }
}
