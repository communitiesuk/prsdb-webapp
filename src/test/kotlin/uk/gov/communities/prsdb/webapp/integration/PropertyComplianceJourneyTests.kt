package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafeEngineerNumPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyIssueDatePagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyOutdatedPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.TaskListPagePropertyCompliance
import kotlin.test.assertContains

@Sql("/data-local.sql")
class PropertyComplianceJourneyTests : IntegrationTest() {
    @Nested
    inner class JourneyTests {
        @Test
        fun `User can navigate whole journey if pages are filled in correctly (in-date gas safety cert)`(page: Page) {
            // Start page
            val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
            assertThat(startPage.heading).containsText("Compliance certificates")
            startPage.startButton.clickAndWait()
            val taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

            // Task List page
            taskListPage.clickUploadTaskWithName("Upload the gas safety certificate")
            val gasSafetyPage = assertPageIs(page, GasSafetyPagePropertyCompliance::class, urlArguments)

            // Gas Safety page
            gasSafetyPage.submitHasGasSafetyCert()
            val gasSafetyIssueDatePage = assertPageIs(page, GasSafetyIssueDatePagePropertyCompliance::class, urlArguments)

            // Gas Safety Cert. Issue Date page
            gasSafetyIssueDatePage.submitDate(currentDate)
            val gasSafeEngineerNumPage = assertPageIs(page, GasSafeEngineerNumPagePropertyCompliance::class, urlArguments)

            // Gas Safe Engineer Num. page
            gasSafeEngineerNumPage.submitEngineerNum("1234567")

            // TODO PRSD-945: Continue journey tests
            assertContains(
                page.url(),
                PropertyComplianceController.getPropertyCompliancePath(PROPERTY_OWNERSHIP_ID) +
                    "/${PropertyComplianceStepId.GasSafetyUpload.urlPathSegment}",
            )
        }

        @Test
        fun `User can navigate whole journey if pages are filled in correctly (outdated gas safety cert)`(page: Page) {
            // Start page
            val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
            assertThat(startPage.heading).containsText("Compliance certificates")
            startPage.startButton.clickAndWait()
            val taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

            // Task List page
            taskListPage.clickUploadTaskWithName("Upload the gas safety certificate")
            val gasSafetyPage = assertPageIs(page, GasSafetyPagePropertyCompliance::class, urlArguments)

            // Gas Safety page
            gasSafetyPage.submitHasGasSafetyCert()
            val gasSafetyIssueDatePage = assertPageIs(page, GasSafetyIssueDatePagePropertyCompliance::class, urlArguments)

            // Gas Safety Cert Issue Date page
            val outdatedIssueDate = currentDate.minus(DatePeriod(years = 1))
            gasSafetyIssueDatePage.submitDate(outdatedIssueDate)
            val gasSafetyOutdatedPage = assertPageIs(page, GasSafetyOutdatedPagePropertyCompliance::class, urlArguments)

            // Gas Safety Outdated page
            assertThat(gasSafetyOutdatedPage.heading).containsText("Your gas safety certificate is out of date")
            gasSafetyOutdatedPage.saveAndReturnToTaskListButton.clickAndWait()
            assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

            // TODO PRSD-954: Continue journey test
        }

        @Test
        fun `User can navigate whole journey if pages are filled in correctly (no gas safety cert)`(page: Page) {
            // Start page
            val startPage = navigator.goToPropertyComplianceStartPage(PROPERTY_OWNERSHIP_ID)
            assertThat(startPage.heading).containsText("Compliance certificates")
            startPage.startButton.clickAndWait()
            val taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

            // Task List page
            taskListPage.clickUploadTaskWithName("Upload the gas safety certificate")
            val gasSafetyPage = assertPageIs(page, GasSafetyPagePropertyCompliance::class, urlArguments)

            // Gas Safety page
            gasSafetyPage.submitHasNoGasSafetyCert()

            // TODO PRSD-949: Continue journey tests
            assertContains(
                page.url(),
                PropertyComplianceController.getPropertyCompliancePath(PROPERTY_OWNERSHIP_ID) +
                    "/${PropertyComplianceStepId.GasSafetyExemption.urlPathSegment}",
            )
        }
    }

    @Nested
    inner class GasSafetyStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val gasSafetyPage = navigator.goToPropertyComplianceGasSafetyPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyPage.form.submit()
            assertThat(gasSafetyPage.form.getErrorMessage()).containsText("Select whether you have a gas safety certificate")
        }
    }

    @Nested
    inner class GasSafetyIssueDateStepTests {
        @ParameterizedTest(name = "{0}")
        @MethodSource("uk.gov.communities.prsdb.webapp.integration.PropertyComplianceJourneyTests#provideInvalidDateStrings")
        fun `Submitting returns a corresponding error when`(
            dayMonthYear: Triple<String, String, String>,
            expectedErrorMessage: String,
        ) {
            val (day, month, year) = dayMonthYear

            val gasSafetyIssueDatePage = navigator.goToPropertyComplianceGasSafetyIssueDatePage(PROPERTY_OWNERSHIP_ID)
            gasSafetyIssueDatePage.submitDate(day, month, year)
            assertThat(gasSafetyIssueDatePage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }
    }

    @Nested
    inner class GasSafetyEngineerNumStepTests {
        @Test
        fun `Submitting with no value entered returns an error`() {
            val gasSafeEngineerNumPage = navigator.goToPropertyComplianceGasSafetyEngineerNumPage(PROPERTY_OWNERSHIP_ID)
            gasSafeEngineerNumPage.form.submit()
            assertThat(gasSafeEngineerNumPage.form.getErrorMessage())
                .containsText("You need to enter a Gas Safe engineer's registered number.")
        }

        @Test
        fun `Submitting with an invalid value entered returns an error`() {
            val gasSafeEngineerNumPage = navigator.goToPropertyComplianceGasSafetyEngineerNumPage(PROPERTY_OWNERSHIP_ID)
            gasSafeEngineerNumPage.submitEngineerNum("ABCDEFG")
            assertThat(gasSafeEngineerNumPage.form.getErrorMessage()).containsText("Enter a 7-digit number.")
        }
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 1L
        private val urlArguments = mapOf("propertyOwnershipId" to PROPERTY_OWNERSHIP_ID.toString())

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
                Arguments.of(Named.of("day and month missing", Triple("", "", "1990")), "You must include a day and a month"),
                Arguments.of(Named.of("month and year missing", Triple("12", "", "")), "You must include a month and a year"),
                Arguments.of(Named.of("day and year missing", Triple("", "11", "")), "You must include a day and a year"),
                // Blank and invalid fields
                Arguments.of(Named.of("day missing (other fields invalid)", Triple("", "0", "0")), "You must include a day"),
                Arguments.of(Named.of("month missing (other fields invalid)", Triple("0", "", "0")), "You must include a month"),
                Arguments.of(Named.of("year missing (other fields invalid)", Triple("0", "0", "")), "You must include a year"),
                Arguments.of(Named.of("day and month missing (year invalid)", Triple("", "", "0")), "You must include a day and a month"),
                Arguments.of(Named.of("month and year missing (day invalid)", Triple("0", "", "")), "You must include a month and a year"),
                Arguments.of(Named.of("day and year missing (month invalid)", Triple("", "0", "")), "You must include a day and a year"),
                // Invalid fields
                Arguments.of(Named.of("invalid day", Triple("0", "11", "1990")), INVALID_DAY_ERR),
                Arguments.of(Named.of("invalid month", Triple("12", "0", "1990")), INVALID_MONTH_ERR),
                Arguments.of(Named.of("invalid year", Triple("12", "11", "0")), INVALID_YEAR_ERR),
                Arguments.of(Named.of("invalid day and month", Triple("32", "0", "1990")), "$INVALID_DAY_ERR. $INVALID_MONTH_ERR"),
                Arguments.of(Named.of("invalid month and year", Triple("12", "13", "0")), "$INVALID_MONTH_ERR. $INVALID_YEAR_ERR"),
                Arguments.of(Named.of("invalid day and year", Triple("0", "11", "1899")), "$INVALID_DAY_ERR. $INVALID_YEAR_ERR"),
                Arguments.of(Named.of("invalid fields", Triple("0", "0", "0")), "$INVALID_DAY_ERR. $INVALID_MONTH_ERR. $INVALID_YEAR_ERR"),
                // Invalid date
                Arguments.of(Named.of("invalid date", Triple("31", "11", "1990")), "You must enter a real date"),
                Arguments.of(Named.of("invalid leap date", Triple("29", "02", "2005")), "You must enter a real date"),
                // Not today or past date
                Arguments.of(Named.of("not today or past date", futureDate), "The date must be today or in the past"),
            )
    }
}
