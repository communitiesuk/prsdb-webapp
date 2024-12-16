package uk.gov.communities.prsdb.webapp.validation

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.MockedConstruction
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper

class DateValidatorTest {
    val dateValidator = DateValidator()

    @Nested
    inner class IsAllBlank {
        @Test
        fun `isAllBlank returns true if all date fields are blank`() {
            Assertions.assertTrue(dateValidator.isAllBlank("", "", ""))
        }

        @ParameterizedTest
        @CsvSource(
            "1,'',''",
            "'',2,''",
            "'','',3",
            "1,2,''",
            "'',2,3",
            "1,'',3",
            "1,2,3",
        )
        fun `isAllBlank returns false if not all are blank`(
            day: String,
            month: String,
            year: String,
        ) {
            Assertions.assertFalse(dateValidator.isAllBlank(day, month, year))
        }
    }

    @Nested
    inner class IsBothBlank {
        @Test
        fun `isBothBlank returns true if all date fields are blank`() {
            Assertions.assertTrue(dateValidator.isBothBlank("", ""))
        }

        @ParameterizedTest
        @CsvSource(
            "1,''",
            "'',2",
            "1,2",
        )
        fun `isBothBlank returns false if not all are blank`(
            firstValue: String,
            secondValue: String,
        ) {
            Assertions.assertFalse(dateValidator.isBothBlank(firstValue, secondValue))
        }
    }

    @Nested
    inner class IsValidDateOrIncomplete {
        @ParameterizedTest
        @CsvSource(
            "12,11,1990",
            "29,02,2004",
        )
        fun `isValidDateOrIncomplete returns true for valid dates`(
            day: String,
            month: String,
            year: String,
        ) {
            Assertions.assertTrue(dateValidator.isValidDate(day, month, year))
        }

        @ParameterizedTest
        @CsvSource(
            "31,11,1990",
            "29,02,2005",
            "30,02,2004",
        )
        fun `isValidDateOrIncomplete returns false if date is not valid`(
            day: String,
            month: String,
            year: String,
        ) {
            Assertions.assertFalse(dateValidator.isValidDate(day, month, year))
        }
    }

    @Nested
    inner class IsDayOrMonthOrYearNotValid {
        @ParameterizedTest
        @CsvSource(
            "'',11,1990",
            "12,'',1990",
            "12,11,''",
            "32,11,1990",
            "12,13,1990",
            "12,11,190",
            "12,11,19900",
        )
        fun `isDayOrMonthOrYearNotValid returns true if day, month or year are not valid or are blank`(
            day: String,
            month: String,
            year: String,
        ) {
            Assertions.assertTrue(dateValidator.isDayOrMonthOrYearNotValid(day, month, year))
        }
    }

    @Nested
    inner class GetAgeFromDate {
        val currentDate = LocalDate(2024, 3, 8)

        lateinit var mockedDateTimeHelper: MockedConstruction<DateTimeHelper>

        @BeforeEach
        fun setUp() {
            mockedDateTimeHelper =
                Mockito.mockConstruction(
                    DateTimeHelper::class.java,
                ) { mock, _ -> whenever(mock.getCurrentDateInUK()).thenReturn(currentDate) }
        }

        @AfterEach
        fun tearDown() {
            mockedDateTimeHelper.close()
        }

        @ParameterizedTest
        @CsvSource(
            "07,03,1990,34",
            "08,03,1990,34",
            "09,03,1990,33",
        )
        fun `getAgeFromDate returns expected value`(
            day: String,
            month: String,
            year: String,
            expectedAge: Int,
        ) {
            Assertions.assertEquals(dateValidator.getAgeFromDate(day, month, year), expectedAge)
        }
    }
}
