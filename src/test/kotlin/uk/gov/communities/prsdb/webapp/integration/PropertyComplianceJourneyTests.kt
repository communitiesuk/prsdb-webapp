package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.GasSafetyPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.TaskListPagePropertyCompliance
import kotlin.test.assertContains

@Sql("/data-local.sql")
class PropertyComplianceJourneyTests : IntegrationTest() {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Nested
    inner class JourneyTests {
        @Test
        fun `User can navigate whole journey if pages are filled in correctly (gas safety cert)`(page: Page) {
            // Start page
            val startPage = navigator.goToPropertyComplianceStartPage(propertyOwnershipId)
            assertThat(startPage.heading).containsText("Compliance certificates")
            startPage.startButton.clickAndWait()
            val taskListPage = assertPageIs(page, TaskListPagePropertyCompliance::class, urlArguments)

            // Task List page
            taskListPage.clickUploadTaskWithName("Upload the gas safety certificate")
            val gasSafetyPage = assertPageIs(page, GasSafetyPagePropertyCompliance::class, urlArguments)

            // Gas Safety page
            gasSafetyPage.submitHasGasSafetyCert()

            // TODO PRSD-943: Continue journey tests
            assertContains(
                page.url(),
                PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                    "/${PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment}",
            )
        }

        @Test
        fun `User can navigate whole journey if pages are filled in correctly (no gas safety cert)`(page: Page) {
            // Start page
            val startPage = navigator.goToPropertyComplianceStartPage(propertyOwnershipId)
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
                PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId) +
                    "/${PropertyComplianceStepId.GasSafetyExemption.urlPathSegment}",
            )
        }
    }

    @Nested
    inner class GasSafetyStepTests {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val gasSafetyPage = navigator.goToPropertyComplianceGasSafetyPage(propertyOwnershipId)
            gasSafetyPage.form.submit()
            assertThat(gasSafetyPage.form.getErrorMessage()).containsText("Select whether you have a gas safety certificate")
        }
    }
}
