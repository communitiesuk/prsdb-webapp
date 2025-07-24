package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.REG_NUM_BASE
import uk.gov.communities.prsdb.webapp.constants.REG_NUM_LENGTH
import uk.gov.communities.prsdb.webapp.constants.REG_NUM_SEG_LENGTH
import uk.gov.communities.prsdb.webapp.constants.SAFE_CHARACTERS_CHARSET
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber

data class RegistrationNumberDataModel(
    val type: RegistrationNumberType,
    val number: Long,
) {
    companion object {
        fun parseTypeOrNull(
            regNumString: String,
            type: RegistrationNumberType,
        ): RegistrationNumberDataModel? =
            try {
                parse(regNumString).let { regNum -> if (regNum.isType(type)) regNum else null }
            } catch (_: Exception) {
                null
            }

        fun fromRegistrationNumber(regNum: RegistrationNumber) = RegistrationNumberDataModel(regNum.type, regNum.number)

        private fun parse(regNumString: String): RegistrationNumberDataModel {
            val baseRegNumString = getBaseRegNumString(regNumString)

            validateBaseRegNumString(baseRegNumString)

            val regNumType = RegistrationNumberType.initialToType(baseRegNumString[0])

            var regNumNumber = 0L
            for (char in baseRegNumString.substring(1)) {
                regNumNumber = REG_NUM_BASE * regNumNumber + SAFE_CHARACTERS_CHARSET.indexOf(char)
            }

            return RegistrationNumberDataModel(regNumType, regNumNumber)
        }

        private fun getBaseRegNumString(regNumString: String): String =
            regNumString.filter { it.isLetterOrDigit() }.uppercase()

        private fun validateBaseRegNumString(baseRegNumString: String) {
            if (baseRegNumString.length != REG_NUM_LENGTH + 1) {
                throw IllegalArgumentException("Invalid registration number string length")
            }
            if (baseRegNumString.substring(1).any { !SAFE_CHARACTERS_CHARSET.contains(it) }) {
                throw IllegalArgumentException("Invalid registration number string characters")
            }
        }
    }

    override fun toString(): String {
        var regNumString = ""
        var quotient = this.number
        while (quotient > 0) {
            regNumString = SAFE_CHARACTERS_CHARSET[(quotient % REG_NUM_BASE).toInt()] + regNumString
            quotient /= REG_NUM_BASE
        }
        regNumString = regNumString.padStart(REG_NUM_LENGTH, SAFE_CHARACTERS_CHARSET[0])

        return this.type.toInitial() +
                "-" +
                regNumString.substring(0, REG_NUM_SEG_LENGTH) +
                "-" +
                regNumString.substring(REG_NUM_SEG_LENGTH)
    }

    private fun isType(type: RegistrationNumberType) = this.type == type
}
