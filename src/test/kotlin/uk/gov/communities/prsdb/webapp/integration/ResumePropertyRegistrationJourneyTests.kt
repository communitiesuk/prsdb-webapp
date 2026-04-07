package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.TaskListPagePropertyRegistration
import kotlin.test.assertFalse

class ResumePropertyRegistrationJourneyTests :
    IntegrationTestWithMutableData("data-mockuser-landlord-with-one-incomplete-property.sql") {
    @Test
    fun `resuming an incomplete property registration via the continue link restores saved journey state`(page: Page) {
        val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
        incompletePropertiesPage.firstSummaryCard.continueLink.clickAndWait()

        val taskListPage = assertPageIs(page, TaskListPagePropertyRegistration::class)

        assertThat(taskListPage.heading).containsText("Register a property")
        assertFalse(
            taskListPage.taskHasStatus("Enter the property address", "Not started"),
            "Property address task should not be 'Not started' after restoring saved journey state",
        )
    }
}
