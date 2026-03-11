package uk.gov.communities.prsdb.webapp.testHelpers.parameterProviders

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.provider.Arguments
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper

class TodayOrPastDateValidationTestParameterProvider {
    companion object {
        private val currentDate = DateTimeHelper().getCurrentDateInUK()
        private val futureDate =
            currentDate.plus(DatePeriod(days = 1)).let {
                Triple(it.dayOfMonth.toString(), it.monthNumber.toString(), it.year.toString())
            }

        private const val INVALID_DAY_ERR = "Day must be a whole number between 1 and 31"
        private const val INVALID_MONTH_ERR = "Month must be a whole number between 1 and 12"
        private const val INVALID_YEAR_ERR = "Year must be a whole number greater than 1899"

        @JvmStatic
        private fun provideInvalidDateStrings() =
            arrayOf(
                // Blank fields
                Arguments.of(Named.of("all fields missing", Triple("", "", "")), "Enter a date"),
                Arguments.of(Named.of("day missing", Triple("", "11", "1990")), "You must include a day"),
                Arguments.of(Named.of("month missing", Triple("12", "", "1990")), "You must include a month"),
                Arguments.of(Named.of("year missing", Triple("12", "11", "")), "You must include a year"),
                Arguments.of(
                    Named.of("day and month missing", Triple("", "", "1990")),
                    "You must include a day and a month",
                ),
                Arguments.of(
                    Named.of("month and year missing", Triple("12", "", "")),
                    "You must include a month and a year",
                ),
                Arguments.of(
                    Named.of("day and year missing", Triple("", "11", "")),
                    "You must include a day and a year",
                ),
                // Blank and invalid fields
                Arguments.of(
                    Named.of("day missing (other fields invalid)", Triple("", "0", "0")),
                    "You must include a day",
                ),
                Arguments.of(
                    Named.of("month missing (other fields invalid)", Triple("0", "", "0")),
                    "You must include a month",
                ),
                Arguments.of(
                    Named.of("year missing (other fields invalid)", Triple("0", "0", "")),
                    "You must include a year",
                ),
                Arguments.of(
                    Named.of("day and month missing (year invalid)", Triple("", "", "0")),
                    "You must include a day and a month",
                ),
                Arguments.of(
                    Named.of("month and year missing (day invalid)", Triple("0", "", "")),
                    "You must include a month and a year",
                ),
                Arguments.of(
                    Named.of("day and year missing (month invalid)", Triple("", "0", "")),
                    "You must include a day and a year",
                ),
                // Invalid fields
                Arguments.of(Named.of("invalid day", Triple("0", "11", "1990")), INVALID_DAY_ERR),
                Arguments.of(Named.of("invalid month", Triple("12", "0", "1990")), INVALID_MONTH_ERR),
                Arguments.of(Named.of("invalid year", Triple("12", "11", "0")), INVALID_YEAR_ERR),
                Arguments.of(
                    Named.of("invalid day and month", Triple("32", "0", "1990")),
                    "$INVALID_DAY_ERR. $INVALID_MONTH_ERR",
                ),
                Arguments.of(
                    Named.of("invalid month and year", Triple("12", "13", "0")),
                    "$INVALID_MONTH_ERR. $INVALID_YEAR_ERR",
                ),
                Arguments.of(
                    Named.of("invalid day and year", Triple("0", "11", "1899")),
                    "$INVALID_DAY_ERR. $INVALID_YEAR_ERR",
                ),
                Arguments.of(
                    Named.of("invalid fields", Triple("0", "0", "0")),
                    "$INVALID_DAY_ERR. $INVALID_MONTH_ERR. $INVALID_YEAR_ERR",
                ),
                // Invalid date
                Arguments.of(Named.of("invalid date", Triple("31", "11", "1990")), "You must enter a real date"),
                Arguments.of(Named.of("invalid leap date", Triple("29", "02", "2005")), "You must enter a real date"),
                // Not today or past date
                Arguments.of(Named.of("not today or past date", futureDate), "The date must be today or in the past"),
            )
    }
}
