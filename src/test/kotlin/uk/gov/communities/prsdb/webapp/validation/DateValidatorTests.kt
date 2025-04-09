package uk.gov.communities.prsdb.webapp.validation

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.FieldSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateValidatorTests {
    companion object {
        private val provideOneBlankDateFieldSets =
            arrayOf(
                argumentSet("blank day", "", "2", "2000"),
                argumentSet("blank month", "1", "", "2000"),
                argumentSet("blank year", "1", "2", ""),
            )

        private val providePartlyBlankDateFieldSets =
            provideOneBlankDateFieldSets +
                arrayOf(
                    argumentSet("blank day and month", "", "", "2000"),
                    argumentSet("blank day and year", "", "2", ""),
                    argumentSet("blank month and year", "1", "", ""),
                )

        @JvmStatic
        private val provideNotAllBlankDateFieldSets =
            providePartlyBlankDateFieldSets + argumentSet("complete day month and year", "1", "2", "2000")

        @JvmStatic
        private val provideNotAllCompleteDateFieldSets =
            providePartlyBlankDateFieldSets + argumentSet("blank day month and year", "", "", "")

        @JvmStatic
        private val provideNotBothBlankFieldSets =
            arrayOf(
                argumentSet("blank second value", "1", ""),
                argumentSet("blank first value", "", "2"),
                argumentSet("no blank values", "1", "2"),
            )

        @JvmStatic
        private val provideInvalidDateFieldSets =
            provideOneBlankDateFieldSets +
                arrayOf(
                    argumentSet("invalid day", "32", "2", "2000"),
                    argumentSet("invalid month", "1", "13", "2000"),
                    argumentSet("invalid year", "1", "2", "1899"),
                )

        @JvmStatic
        private val provideRealDateFieldSets =
            arrayOf(
                argumentSet("valid date", "12", "11", "1990"),
                argumentSet("valid leap date", "29", "02", "2004"),
            )

        @JvmStatic
        private val provideFakeDateFieldSets =
            arrayOf(
                argumentSet("invalid date", "31", "11", "1990"),
                argumentSet("invalid leap date", "29", "02", "2005"),
            )
    }

    @Nested
    inner class IsAllBlank {
        @Test
        fun `isAllBlank returns true if blank day month and year`() {
            assertTrue(DateValidator.isAllBlank("", "", ""))
        }

        @ParameterizedTest(name = "{argumentSetName}")
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
        @ParameterizedTest(name = "{argumentSetName}")
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
        @ParameterizedTest(name = "{argumentSetName}")
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
