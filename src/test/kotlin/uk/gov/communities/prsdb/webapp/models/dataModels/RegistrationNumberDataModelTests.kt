package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
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
                Arguments.of(
                    Named.of("expected format - LRN", "L-CCCC-CCCC"),
                    RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MIN_REG_NUM),
                ),
                Arguments.of(
                    Named.of("expected format - PRN", "P-9999-9999"),
                    RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, MAX_REG_NUM),
                ),
                Arguments.of(
                    Named.of("expected format - ARN", "A-CCCC-CCCC"),
                    RegistrationNumberDataModel(RegistrationNumberType.AGENT, MIN_REG_NUM),
                ),
                Arguments.of(
                    Named.of("unexpected segmentation - none", "L99999999"),
                    RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MAX_REG_NUM),
                ),
                Arguments.of(
                    Named.of("unexpected segmentation - too much", "L-999-999-99"),
                    RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MAX_REG_NUM),
                ),
                Arguments.of(
                    Named.of("unexpected segmenting symbol", "L 9999 9999"),
                    RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MAX_REG_NUM),
                ),
                Arguments.of(
                    Named.of("unexpected case", "l-cccc-cccc"),
                    RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MIN_REG_NUM),
                ),
            )

        @JvmStatic
        fun provideNonParseableRegNumStrings() =
            listOf(
                Named.of("invalid registration number type", "9-9999-9999"),
                Named.of("invalid registration number characters - number not in charset", "L-1111-1111"),
                Named.of("invalid registration number characters - letter not in charset", "L-LLLL-LLLL"),
                Named.of("invalid registration number length - too long", "L-9999-9999-9"),
                Named.of("invalid registration number length - too short", "L-9999"),
            )

        @JvmStatic
        fun provideRegNumsAndStrings() =
            listOf(
                Arguments.of(
                    Named.of("LRN", RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, MIN_REG_NUM)),
                    "L-CCCC-CCCC",
                ),
                Arguments.of(
                    Named.of("PRN", RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, MAX_REG_NUM)),
                    "P-9999-9999",
                ),
                Arguments.of(
                    Named.of("ARN", RegistrationNumberDataModel(RegistrationNumberType.AGENT, MAX_REG_NUM)),
                    "A-9999-9999",
                ),
            )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideParseableStringsAndRegNums")
    fun `parseTypeOrNull returns a corresponding registration number data model when given an`(
        parseableString: String,
        expectedRegNum: RegistrationNumberDataModel,
    ) {
        assertEquals(RegistrationNumberDataModel.parseTypeOrNull(parseableString, expectedRegNum.type), expectedRegNum)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideNonParseableRegNumStrings")
    fun `parseTypeOrNull returns null when given an`(nonParseableString: String) {
        val anyType = RegistrationNumberType.LANDLORD

        assertNull(RegistrationNumberDataModel.parseTypeOrNull(nonParseableString, anyType))
    }

    @Test
    fun `parseTypeOrNull returns null when the given registration number is not of the given type`() {
        val landlordRegNumString = "L-CCCC-CCCC"

        assertNull(RegistrationNumberDataModel.parseTypeOrNull(landlordRegNumString, RegistrationNumberType.PROPERTY))
    }

    @Test
    fun `fromRegistrationNumber returns an equivalent data model`() {
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, MAX_REG_NUM)
        val expectedRegNumDataModel = RegistrationNumberDataModel(registrationNumber.type, registrationNumber.number)

        val registrationNumberDataModel = RegistrationNumberDataModel.fromRegistrationNumber(registrationNumber)

        assertEquals(expectedRegNumDataModel, registrationNumberDataModel)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideRegNumsAndStrings")
    fun `toString returns a correctly formatted registration number string when given a`(
        regNum: RegistrationNumberDataModel,
        expectedString: String,
    ) {
        assertEquals(regNum.toString(), expectedString)
    }
}
