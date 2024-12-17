package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.REG_NUM_BASE
import uk.gov.communities.prsdb.webapp.constants.REG_NUM_CHARSET
import uk.gov.communities.prsdb.webapp.constants.REG_NUM_LENGTH
import uk.gov.communities.prsdb.webapp.constants.REG_NUM_SEG_LENGTH
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber

data class RegistrationNumberDataModel(
    val type: RegistrationNumberType,
    val number: Long,
) {
    companion object {
        fun parseOrNull(regNumString: String): RegistrationNumberDataModel? =
            try {
                parse(regNumString)
            } catch (_: Exception) {
                null
            }

        fun parse(regNumString: String): RegistrationNumberDataModel {
            val baseRegNumString = getBaseRegNumString(regNumString)

            validateBaseRegNumString(baseRegNumString)

            val regNumType = RegistrationNumberType.initialToType(baseRegNumString[0])

            var regNumNumber = 0L
            for (char in baseRegNumString.substring(1)) {
                regNumNumber = REG_NUM_BASE * regNumNumber + REG_NUM_CHARSET.indexOf(char)
            }

            return RegistrationNumberDataModel(regNumType, regNumNumber)
        }

        fun fromRegistrationNumber(regNum: RegistrationNumber) = RegistrationNumberDataModel(regNum.type, regNum.number!!)

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

    override fun toString(): String {
        var regNumString = ""
        var quotient = this.number
        while (quotient > 0) {
            regNumString = REG_NUM_CHARSET[(quotient % REG_NUM_BASE).toInt()] + regNumString
            quotient /= REG_NUM_BASE
        }
        regNumString = regNumString.padStart(REG_NUM_LENGTH, REG_NUM_CHARSET[0])

        return this.type.toInitial() +
            "-" +
            regNumString.substring(0, REG_NUM_SEG_LENGTH) +
            "-" +
            regNumString.substring(REG_NUM_SEG_LENGTH)
    }

    fun isType(type: RegistrationNumberType) = this.type == type
}
