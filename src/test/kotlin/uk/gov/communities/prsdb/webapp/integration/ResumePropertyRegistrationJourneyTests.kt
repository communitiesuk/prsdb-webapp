package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordIncompletePropertiesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LookupAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.TaskListPagePropertyRegistration
import kotlin.test.assertFalse

class ResumePropertyRegistrationJourneyTests :
    IntegrationTestWithMutableData("data-mockuser-landlord-with-one-incomplete-property.sql") {
    @Test
    fun `resuming an incomplete property registration via the continue link restores saved journey state`(page: Page) {
        val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
        incompletePropertiesPage.firstSummaryCard.continueLink.clickAndWait()

        val taskListPage = assertPageIs(page, TaskListPagePropertyRegistration::class)

        assertFalse(
            taskListPage.taskHasStatus("Property address", "Not started"),
            "Property address task should not be 'Not started' after restoring saved journey state",
        )
    }

    @Test
    fun `task list back link navigates to incomplete properties page after resuming registration`(page: Page) {
        val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
        incompletePropertiesPage.firstSummaryCard.continueLink.clickAndWait()
        val taskListPage = assertPageIs(page, TaskListPagePropertyRegistration::class)

        taskListPage.backLink.clickAndWait()
        assertPageIs(page, LandlordIncompletePropertiesPage::class)
    }

    @Test
    fun `task list back link navigates to incomplete properties page after resuming registration and returning from a task`(page: Page) {
        val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
        incompletePropertiesPage.firstSummaryCard.continueLink.clickAndWait()
        var taskListPage = assertPageIs(page, TaskListPagePropertyRegistration::class)

        taskListPage.clickRegisterTaskWithName("Property address")
        assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)

        val backLink = BackLink.default(page)
        backLink.clickAndWait()
        taskListPage = assertPageIs(page, TaskListPagePropertyRegistration::class)

        taskListPage.backLink.clickAndWait()
        assertPageIs(page, LandlordIncompletePropertiesPage::class)
    }
}
