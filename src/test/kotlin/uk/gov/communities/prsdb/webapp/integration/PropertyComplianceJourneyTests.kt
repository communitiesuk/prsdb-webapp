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
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafeEngineerNumPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionConfirmationPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionOtherReasonPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyExemptionReasonPagePropertyCompliance
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
        fun `User can navigate whole journey if pages are filled in correctly (no gas safety cert, exemption)`(page: Page) {
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
            val gasSafetyExemptionPage = assertPageIs(page, GasSafetyExemptionPagePropertyCompliance::class, urlArguments)

            // Gas Safety Exemption page
            gasSafetyExemptionPage.submitHasGasSafetyCertExemption()
            val gasSafetyExemptionReasonPage = assertPageIs(page, GasSafetyExemptionReasonPagePropertyCompliance::class, urlArguments)

            // Gas Safety Exemption Reason page
            gasSafetyExemptionReasonPage.submitExemptionReason(GasSafetyExemptionReason.NO_GAS_SUPPLY)
            val gasSafetyExemptionConfirmationPage =
                assertPageIs(page, GasSafetyExemptionConfirmationPagePropertyCompliance::class, urlArguments)

            // Gas Safety Exemption Confirmation page
            assertThat(gasSafetyExemptionConfirmationPage.heading)
                .containsText("You’ve marked this property as not needing a gas safety certificate")
            gasSafetyExemptionConfirmationPage.saveAndReturnToTaskListButton.clickAndWait()
            assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

            // TODO PRSD-954: Continue journey test
        }

        @Test
        fun `User can navigate whole journey if pages are filled in correctly (no gas safety cert, no exemption)`(page: Page) {
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
            val gasSafetyExemptionPage = assertPageIs(page, GasSafetyExemptionPagePropertyCompliance::class, urlArguments)

            // Gas Safety Exemption page
            gasSafetyExemptionPage.submitHasNoGasSafetyCertExemption()

            // TODO PRSD-952: Continue journey tests
            assertContains(
                page.url(),
                PropertyComplianceController.getPropertyCompliancePath(PROPERTY_OWNERSHIP_ID) +
                    "/${PropertyComplianceStepId.GasSafetyExemptionMissing.urlPathSegment}",
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

    @Nested
    inner class GasSafetyExemptionStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val gasSafetyExemptionPage = navigator.goToPropertyComplianceGasSafetyExemptionPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionPage.form.submit()
            assertThat(gasSafetyExemptionPage.form.getErrorMessage())
                .containsText("Select whether you have a gas safety certificate exemption")
        }
    }

    @Nested
    inner class GasSafetyExemptionReasonStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val gasSafetyExemptionReasonPage = navigator.goToPropertyComplianceGasSafetyExemptionReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionReasonPage.form.submit()
            assertThat(gasSafetyExemptionReasonPage.form.getErrorMessage())
                .containsText("Select why this property is exempt from gas safety")
        }

        @Test
        fun `Submitting with 'other' selected redirects to the gas safety exemption other reason page`(page: Page) {
            val gasSafetyExemptionReasonPage = navigator.goToPropertyComplianceGasSafetyExemptionReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionReasonPage.submitExemptionReason(GasSafetyExemptionReason.OTHER)
            assertPageIs(page, GasSafetyExemptionOtherReasonPagePropertyCompliance::class, urlArguments)
        }
    }

    @Nested
    inner class GasSafetyExemptionOtherReasonStepTests {
        @Test
        fun `Submitting with no reason returns an error`(page: Page) {
            val gasSafetyExemptionOtherReasonPage = navigator.goToPropertyComplianceGasSafetyExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionOtherReasonPage.form.submit()
            assertThat(gasSafetyExemptionOtherReasonPage.form.getErrorMessage())
                .containsText("Explain why your property is exempt from having a gas safety certificate")
        }

        @Test
        fun `Submitting with a too long reason returns an error`(page: Page) {
            val gasSafetyExemptionOtherReasonPage = navigator.goToPropertyComplianceGasSafetyExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionOtherReasonPage.submitReason("too long reason".repeat(GAS_SAFETY_EXEMPTION_OTHER_REASON_MAX_LENGTH))
            assertThat(gasSafetyExemptionOtherReasonPage.form.getErrorMessage("otherReason"))
                .containsText("Explanation must be 200 characters or fewer")
        }

        @Test
        fun `Submitting with a valid reason redirects to the gas safety exemption confirmation page`(page: Page) {
            val gasSafetyExemptionOtherReasonPage = navigator.goToPropertyComplianceGasSafetyExemptionOtherReasonPage(PROPERTY_OWNERSHIP_ID)
            gasSafetyExemptionOtherReasonPage.submitReason("valid reason")

            // TODO PRSD-951: Replace with gas exemption confirmation page
            assertContains(
                page.url(),
                PropertyComplianceController.getPropertyCompliancePath(PROPERTY_OWNERSHIP_ID) +
                    "/${PropertyComplianceStepId.GasSafetyExemptionConfirmation.urlPathSegment}",
            )
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
