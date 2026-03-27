package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.JointLandlordsPropertyRegistrationStrategy
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.JointLandlordsTask

class JointLandlordsPropertyRegistrationStrategyTests : FeatureFlagTest() {
    @Autowired
    lateinit var strategy: JointLandlordsPropertyRegistrationStrategy

    @Test
    fun `when feature is disabled ifEnabled does not execute the action`() {
        featureFlagManager.disableFeature(JOINT_LANDLORDS)
        var actionExecuted = false

        strategy.ifEnabled { actionExecuted = true }

        assertFalse(actionExecuted)
    }

    @Test
    fun `when feature is enabled ifEnabled executes the action`() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
        var actionExecuted = false

        strategy.ifEnabled { actionExecuted = true }

        assertTrue(actionExecuted)
    }

    @Test
    fun `when feature is disabled ifEnabledOrElse returns the ifDisabled result`() {
        featureFlagManager.disableFeature(JOINT_LANDLORDS)

        val result = strategy.ifEnabledOrElse(ifEnabled = { "enabled" }, ifDisabled = { "disabled" })

        assertTrue(result == "disabled")
    }

    @Test
    fun `when feature is enabled ifEnabledOrElse returns the ifEnabled result`() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)

        val result = strategy.ifEnabledOrElse(ifEnabled = { "enabled" }, ifDisabled = { "disabled" })

        assertTrue(result == "enabled")
    }

    @Test
    fun `when feature is disabled getJointLandlordsTaskListItems returns an empty list`() {
        featureFlagManager.disableFeature(JOINT_LANDLORDS)

        val result = strategy.getJointLandlordsTaskListItems(mock<PropertyRegistrationJourneyState>())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `when feature is enabled getJointLandlordsTaskListItems returns task list items`() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
        val mockTask = mock<JointLandlordsTask>()
        whenever(mockTask.taskStatus()).thenReturn(TaskStatus.NOT_STARTED)
        val mockState = mock<PropertyRegistrationJourneyState>()
        whenever(mockState.jointLandlordsTask).thenReturn(mockTask)

        val result = strategy.getJointLandlordsTaskListItems(mockState)

        assertFalse(result.isEmpty())
    }
}
