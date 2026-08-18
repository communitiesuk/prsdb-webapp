package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING

// Covers the property registration journey with the restructure-and-skipping feature flag OFF.
// TODO PDJB-1340: delete every PropertyRegistrationPreRestructure*SinglePageTests file when
// PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed. Every class here has an equivalent
// in the flag-on files, so they can all be removed wholesale.
class PropertyRegistrationPreRestructureTaskListSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun disableRestructureAndSkippingFlag() {
        featureFlagManager.disableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
    }

    @Nested
    inner class TaskListStep {
        @Test
        fun `Completing preceding steps will show a task as not started and completed steps as complete`(page: Page) {
            navigator.skipToPropertyRegistrationHasJointLandlordsPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.taskHasStatus("Property address", "Complete"))
            assert(taskListPage.taskHasStatus("Property type", "Complete"))
            assert(taskListPage.taskHasStatus("How you own the property", "Complete"))
            assert(taskListPage.taskHasStatus("If the property has a license", "Complete"))
            assert(taskListPage.taskHasStatus("Tenancy and rental information", "Complete"))
            assert(taskListPage.taskHasStatus("Invite joint landlords", "Not\u00A0started"))
            assert(taskListPage.taskHasStatus("Gas safety certificate", "Cannot start yet"))
            assert(taskListPage.taskHasStatus("Electrical safety certificate", "Cannot start yet"))
            assert(taskListPage.taskHasStatus("Energy performance certificate (EPC)", "Cannot start yet"))
        }

        @Test
        fun `EPC task (starting with an internal step) shows as Not Started when the user is on the first step they see`(page: Page) {
            navigator.skipToPropertyRegistrationHasEpcPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.taskHasStatus("Energy performance certificate (EPC)", "Not\u00A0started"))
        }

        @Test
        fun `Completing first step of a task will show a task as in progress and completed steps as complete`(page: Page) {
            navigator.skipToPropertyRegistrationRentFrequencyPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.taskHasStatus("Property address", "Complete"))
            assert(taskListPage.taskHasStatus("Property type", "Complete"))
            assert(taskListPage.taskHasStatus("How you own the property", "Complete"))
            assert(taskListPage.taskHasStatus("If the property has a license", "Complete"))
            assert(taskListPage.taskHasStatus("Tenancy and rental information", "In progress"))
            assert(taskListPage.taskHasStatus("Invite joint landlords", "Cannot start yet"))
            assert(taskListPage.taskHasStatus("Gas safety certificate", "Cannot start yet"))
            assert(taskListPage.taskHasStatus("Electrical safety certificate", "Cannot start yet"))
            assert(taskListPage.taskHasStatus("Energy performance certificate (EPC)", "Cannot start yet"))
        }
    }
}
