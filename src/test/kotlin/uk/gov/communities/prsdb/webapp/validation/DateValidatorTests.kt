package uk.gov.communities.prsdb.webapp.validation

import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.FieldSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateValidatorTests {
    companion object {
        private val provideOneBlankDateFieldSets =
            arrayOf(
                Arguments.of(Named.of("blank day", ""), "2", "2000"),
                Arguments.of(Named.of("blank month", "1"), "", "2000"),
                Arguments.of(Named.of("blank year", "1"), "2", ""),
            )

        private val providePartlyBlankDateFieldSets =
            provideOneBlankDateFieldSets +
                arrayOf(
                    Arguments.of(Named.of("blank day and month", ""), "", "2000"),
                    Arguments.of(Named.of("blank day and year", ""), "2", ""),
                    Arguments.of(Named.of("blank month and year", "1"), "", ""),
                )

        @JvmStatic
        private val provideNotAllBlankDateFieldSets =
            providePartlyBlankDateFieldSets + Arguments.of(Named.of("complete day month and year", "1"), "2", "2000")

        @JvmStatic
        private val provideNotAllCompleteDateFieldSets =
            providePartlyBlankDateFieldSets + Arguments.of(Named.of("blank day month and year", ""), "", "")

        @JvmStatic
        private val provideNotBothBlankFieldSets =
            arrayOf(
                Arguments.of(Named.of("blank second value", "1"), ""),
                Arguments.of(Named.of("blank first value", ""), "2"),
                Arguments.of(Named.of("no blank values", "1"), "2"),
            )

        @JvmStatic
        private val provideInvalidDateFieldSets =
            provideOneBlankDateFieldSets +
                arrayOf(
                    Arguments.of(Named.of("invalid day", "32"), "2", "2000"),
                    Arguments.of(Named.of("invalid month", "1"), "13", "2000"),
                    Arguments.of(Named.of("invalid year (too in the past)", "1"), "2", "1899"),
                    Arguments.of(Named.of("invalid year (too in the future)", "1"), "2", "3000"),
                )

        @JvmStatic
        private val provideRealDateFieldSets =
            arrayOf(
                Arguments.of(Named.of("valid date", "12"), "11", "1990"),
                Arguments.of(Named.of("valid leap date", "29"), "02", "2004"),
            )

        @JvmStatic
        private val provideFakeDateFieldSets =
            arrayOf(
                Arguments.of(Named.of("invalid date", "31"), "11", "1990"),
                Arguments.of(Named.of("invalid leap date", "29"), "02", "2005"),
            )
    }

    @Nested
    inner class IsAllBlank {
        @Test
        fun `isAllBlank returns true if blank day month and year`() {
            assertTrue(DateValidator.isAllBlank("", "", ""))
        }

        @ParameterizedTest(name = "{0}")
        @FieldSource("uk.gov.communities.prsdb.webapp.validation.DateValidatorTests#provideNotAllBlankDateFieldSets")
        fun `isAllBlank returns false if`(
            day: String,
            month: String,
            year: String,
        ) {
            assertFalse(DateValidator.isAllBlank(day, month, year))
        }
    }

    @Nested
    inner class IsAnyBlank {
        @ParameterizedTest(name = "{0}")
        @FieldSource("uk.gov.communities.prsdb.webapp.validation.DateValidatorTests#provideNotAllCompleteDateFieldSets")
        fun `isAnyBlank returns true if`(
            day: String,
            month: String,
            year: String,
        ) {
            assertTrue(DateValidator.isAnyBlank(day, month, year))
        }

        @Test
        fun `isAnyBlank returns false if no date field is blank`() {
            assertFalse(DateValidator.isAnyBlank("1", "2", "2000"))
        }
    }

    @Nested
    inner class IsBothBlank {
        @Test
        fun `isBothBlank returns true if both fields are blank`() {
            assertTrue(DateValidator.isBothBlank("", ""))
        }

        @ParameterizedTest
        @FieldSource("uk.gov.communities.prsdb.webapp.validation.DateValidatorTests#provideNotBothBlankFieldSets")
        fun `isBothBlank returns false if not all are blank`(
            firstValue: String,
            secondValue: String,
        ) {
            assertFalse(DateValidator.isBothBlank(firstValue, secondValue))
        }
    }

    @Nested
    inner class IsAnyInvalid {
        @ParameterizedTest(name = "{0}")
        @FieldSource("uk.gov.communities.prsdb.webapp.validation.DateValidatorTests#provideInvalidDateFieldSets")
        fun `isAnyInvalid returns true if`(
            day: String,
            month: String,
            year: String,
        ) {
            assertTrue(DateValidator.isAnyInvalid(day, month, year))
        }

        @Test
        fun `isAnyInvalid returns false if no date field is invalid`() {
            assertFalse(DateValidator.isAnyInvalid("1", "2", "2000"))
        }
    }

    @Nested
    inner class IsValidDate {
        @ParameterizedTest(name = "{0}")
        @FieldSource("uk.gov.communities.prsdb.webapp.validation.DateValidatorTests#provideRealDateFieldSets")
        fun `isValidDate returns true for`(
            day: String,
            month: String,
            year: String,
        ) {
            assertTrue(DateValidator.isValidDate(day, month, year))
        }

        @ParameterizedTest(name = "{0}")
        @FieldSource("uk.gov.communities.prsdb.webapp.validation.DateValidatorTests#provideFakeDateFieldSets")
        fun `isValidDate returns false for`(
            day: String,
            month: String,
            year: String,
        ) {
            assertFalse(DateValidator.isValidDate(day, month, year))
        }
    }
}
