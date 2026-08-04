package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING

class PropertyRegistrationTaskListSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun enableRestructureAndSkippingFlag() {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
    }

    @Nested
    inner class TaskListStep {
        @Test
        fun `Completing preceding steps will show a task as not started and completed steps as complete`(page: Page) {
            navigator.skipToPropertyRegistrationHasJointLandlordsPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.getAboutYourPropertyTask("Property details").statusText.contains("Complete"))
            assert(taskListPage.getAboutYourPropertyTask("Ownership and landlords").statusText.contains("In progress"))
            assert(taskListPage.getAboutYourPropertyTask("Tell us if your property’s occupied").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Tell us if your property needs a license").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Gas safety certificate").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Electrical safety certificate").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Energy performance certificate (EPC)").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Tenancy details").statusText.contains("Cannot start yet"))
            assert(taskListPage.getSubmitYourRegistrationTask("Check and submit your answers").statusText.contains("Cannot start yet"))
        }

        @Test
        fun `EPC task (starting with an internal step) shows as Not Started when the user is on the first step they see`(page: Page) {
            navigator.skipToPropertyRegistrationHasEpcPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.getRentedOutTask("Energy performance certificate (EPC)").statusText.contains("Not started"))
        }

        @Test
        fun `Completing first step of a task will show a task as in progress and completed steps as complete`(page: Page) {
            navigator.skipToPropertyRegistrationHasGasCertPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.getAboutYourPropertyTask("Property details").statusText.contains("Complete"))
            assert(taskListPage.getAboutYourPropertyTask("Ownership and landlords").statusText.contains("Complete"))
            assert(taskListPage.getAboutYourPropertyTask("Tell us if your property’s occupied").statusText.contains("Complete"))
            assert(taskListPage.getRentedOutTask("Tell us if your property needs a license").statusText.contains("Complete"))
            assert(taskListPage.getRentedOutTask("Gas safety certificate").statusText.contains("In progress"))
            assert(taskListPage.getRentedOutTask("Electrical safety certificate").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Energy performance certificate (EPC)").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Tenancy details").statusText.contains("Cannot start yet"))
            assert(taskListPage.getSubmitYourRegistrationTask("Check and submit your answers").statusText.contains("Cannot start yet"))
        }
    }
}
