package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.MAX_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.MIN_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import kotlin.test.assertNull

class RegistrationNumberDataModelTests {
    companion object {
        @JvmStatic
        fun provideParseableStringsAndRegNums() =
            listOf(
                // Expected format
                Arguments.of("L-CCCC-CCCC", RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MIN_REG_NUM)),
                Arguments.of("P-9999-9999", RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, MAX_REG_NUM)),
                Arguments.of("A-CCCC-CCCC", RegistrationNumberDataModel(RegistrationNumberType.AGENT, MIN_REG_NUM)),
                // Unexpected segmentation
                Arguments.of("L99999999", RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MAX_REG_NUM)),
                Arguments.of("L-999-999-99", RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MAX_REG_NUM)),
                // Unexpected segmenting symbol
                Arguments.of("L 9999 9999", RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MAX_REG_NUM)),
                // Unexpected case
                Arguments.of("l-cccc-cccc", RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MIN_REG_NUM)),
            )

        @JvmStatic
        fun provideNonParseableRegNumStrings() =
            listOf(
                // Invalid registration number type
                "9-9999-9999",
                // Invalid registration number characters
                "L-1111-1111",
                "L-LLLL-LLLL",
                // Invalid registration number length
                "L-9999-9999-9",
                "L-9999",
            )

        @JvmStatic
        fun provideRegNumsAndStrings() =
            listOf(
                Arguments.of(RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MIN_REG_NUM), "L-CCCC-CCCC"),
                Arguments.of(RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, MAX_REG_NUM), "P-9999-9999"),
                Arguments.of(RegistrationNumberDataModel(RegistrationNumberType.AGENT, MAX_REG_NUM), "A-9999-9999"),
            )
    }

    @ParameterizedTest
    @MethodSource("provideParseableStringsAndRegNums")
    fun `parse returns a corresponding registration number data model`(
        parseableString: String,
        expectedRegNum: RegistrationNumberDataModel,
    ) {
        assertEquals(RegistrationNumberDataModel.parse(parseableString), expectedRegNum)
    }

    @ParameterizedTest
    @MethodSource("provideNonParseableRegNumStrings")
    fun `parse throws an illegal argument exception when given an invalid registration number`(nonParseableString: String) {
        assertThrows<IllegalArgumentException> { RegistrationNumberDataModel.parse(nonParseableString) }
    }

    @ParameterizedTest
    @MethodSource("provideRegNumsAndStrings")
    fun `toString returns a correctly formatted registration number string`(
        regNum: RegistrationNumberDataModel,
        expectedString: String,
    ) {
        assertEquals(regNum.toString(), expectedString)
    }

    @ParameterizedTest
    @MethodSource("provideParseableStringsAndRegNums")
    fun `parseOrNull returns a corresponding registration number data model`(
        parseableString: String,
        expectedRegNum: RegistrationNumberDataModel,
    ) {
        assertEquals(RegistrationNumberDataModel.parseOrNull(parseableString), expectedRegNum)
    }

    @ParameterizedTest
    @MethodSource("provideNonParseableRegNumStrings")
    fun `parseOrNull returns null when given an invalid registration number`(nonParseableString: String) {
        assertNull(RegistrationNumberDataModel.parseOrNull(nonParseableString))
    }

    @Test
    fun `fromRegistrationNumber returns an equivalent data model`() {
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, MAX_REG_NUM)
        val expectedRegNumDataModel = RegistrationNumberDataModel(registrationNumber.type, registrationNumber.number!!)

        val registrationNumberDataModel = RegistrationNumberDataModel.fromRegistrationNumber(registrationNumber)

        assertEquals(expectedRegNumDataModel, registrationNumberDataModel)
    }
}
