package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyStateSessionBuilder

class PropertyRegistrationTaskListSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun enableFeatureFlags() {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        featureFlagManager.enableFeature(DELEGATE_TO_LETTING_AGENT)
    }

    @Nested
    inner class TaskListStep {
        @Test
        fun `Completing preceding steps will show a task as not started and completed steps as complete`() {
            navigator.skipToPropertyRegistrationHasJointLandlordsPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.getAboutYourPropertyTask("Property details").statusText.contains("Complete"))
            assert(taskListPage.getAboutYourPropertyTask("Ownership and landlords").statusText.contains("In progress"))
            assert(taskListPage.getAboutYourPropertyTask("Tell us if your property’s occupied").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Who will provide these details").statusText.contains("Not\u00A0started"))
            assert(taskListPage.getRentedOutTask("Tell us if your property needs a license").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Gas safety certificate").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Electrical safety certificate").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Energy performance certificate (EPC)").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Tenancy details").statusText.contains("Cannot start yet"))
            assert(taskListPage.getSubmitYourRegistrationTask("Check and submit your answers").statusText.contains("Cannot start yet"))
        }

        @Test
        fun `EPC task (starting with an internal step) shows as Not Started when the user is on the first step they see`() {
            navigator.skipToPropertyRegistrationHasEpcPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.getRentedOutTask("Energy performance certificate (EPC)").statusText.contains("Not\u00A0started"))
        }

        @Test
        fun `Completing first step of a task will show a task as in progress and completed steps as complete`() {
            navigator.skipToPropertyRegistrationHasGasCertPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.getAboutYourPropertyTask("Property details").statusText.contains("Complete"))
            assert(taskListPage.getAboutYourPropertyTask("Ownership and landlords").statusText.contains("Complete"))
            assert(taskListPage.getAboutYourPropertyTask("Tell us if your property’s occupied").statusText.contains("Complete"))
            assert(taskListPage.getRentedOutTask("Who will provide these details").statusText.contains("Not\u00A0started"))
            assert(taskListPage.getRentedOutTask("Tell us if your property needs a license").statusText.contains("Complete"))
            assert(taskListPage.getRentedOutTask("Gas safety certificate").statusText.contains("In progress"))
            assert(taskListPage.getRentedOutTask("Electrical safety certificate").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Energy performance certificate (EPC)").statusText.contains("Cannot start yet"))
            assert(taskListPage.getRentedOutTask("Tenancy details").statusText.contains("Cannot start yet"))
            assert(taskListPage.getSubmitYourRegistrationTask("Check and submit your answers").statusText.contains("Cannot start yet"))
        }
    }

    @Nested
    inner class DelegationToLettingAgent {
        @Test
        fun `Delegation task appears with Not started status for occupied property`() {
            navigator.skipToPropertyRegistrationHasGasCertPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()

            assert(taskListPage.getRentedOutTask("Who will provide these details").statusText.contains("Not\u00A0started"))
        }

        @Test
        fun `Delegation task appears with Not needed yet status for unoccupied property`() {
            val taskListPage = navigator.goToRestructuredPropertyRegistrationTaskListUnoccupied()

            assertEquals("Not\u00A0needed\u00A0yet", taskListPage.getRentedOutTask("Who will provide these details").statusText.trim())
        }
    }

    @Nested
    inner class DelegationToLettingAgentTaskFlagDisabled {
        @BeforeEach
        fun disableDelegateToLettingAgentFlag() {
            featureFlagManager.disableFeature(DELEGATE_TO_LETTING_AGENT)
        }

        @Test
        fun `Delegation task does not appear for occupied property when DELEGATE_TO_LETTING_AGENT feature flag is disabled`() {
            navigator.skipToPropertyRegistrationHasGasCertPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()

            assert("Who will provide these details" !in taskListPage.getRentedOutTaskNames()) {
                "Delegation task should not be visible for occupied property when feature flag is disabled"
            }
        }

        @Test
        fun `Delegation task does not appear for unoccupied property when DELEGATE_TO_LETTING_AGENT feature flag is disabled`() {
            val taskListPage = navigator.goToRestructuredPropertyRegistrationTaskListUnoccupied()

            assert("Who will provide these details" !in taskListPage.getRentedOutTaskNames()) {
                "Delegation task should not be visible for unoccupied property when feature flag is disabled"
            }
        }
    }

    @Nested
    inner class DelegationToLettingAgentTaskFlagEnabled {
        @BeforeEach
        fun enableDelegateToLettingAgentFlag() {
            featureFlagManager.enableFeature(DELEGATE_TO_LETTING_AGENT)
        }

        @Test
        fun `Delegation task does not appear when occupancy has not been set`() {
            val taskListPage =
                navigator.goToRestructuredPropertyRegistrationTaskList(
                    PropertyStateSessionBuilder.beforePropertyRegistrationLicensingType(),
                )

            assert("Who will provide these details" !in taskListPage.getRentedOutTaskNames()) {
                "Delegation task should not be visible when occupancy has not been set"
            }
        }

        @Test
        fun `Delegation task appears with Not started status for occupied property`() {
            navigator.skipToPropertyRegistrationHasGasCertPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()

            val delegationTask = taskListPage.getRentedOutTask("Who will provide these details")
            assert(delegationTask.statusText.contains("Not\u00A0started"))
            assertEquals(
                "If you’re using a letting agent or property manager, they can provide the rest of this section",
                delegationTask.hintText.trim(),
            )
        }

        @Test
        fun `Delegation task appears with Not needed yet status for unoccupied property`() {
            val taskListPage = navigator.goToRestructuredPropertyRegistrationTaskListUnoccupied()

            val delegationTask = taskListPage.getRentedOutTask("Who will provide these details")
            assertEquals("Not\u00A0needed\u00A0yet", delegationTask.statusText.trim())
            assertEquals(
                "Once your property’s occupied, your letting agent or property manager can keep these details updated for you",
                delegationTask.hintText.trim(),
            )
        }
    }
}
