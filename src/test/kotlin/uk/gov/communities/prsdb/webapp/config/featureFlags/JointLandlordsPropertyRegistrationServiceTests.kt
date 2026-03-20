package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.services.interfaces.JointLandlordsPropertyRegistrationService

class JointLandlordsPropertyRegistrationServiceTests : FeatureFlagTest() {
    @Autowired
    lateinit var service: JointLandlordsPropertyRegistrationService

    @Test
    fun `when feature is disabled addJointLandlordsJourneyTaskIfEnabled does not execute the task`() {
        featureFlagManager.disableFeature(JOINT_LANDLORDS)
        var taskExecuted = false

        service.addJointLandlordsJourneyTaskIfEnabled { taskExecuted = true }

        assertFalse(taskExecuted)
    }

    @Test
    fun `when feature is enabled addJointLandlordsJourneyTaskIfEnabled executes the task`() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
        var taskExecuted = false

        service.addJointLandlordsJourneyTaskIfEnabled { taskExecuted = true }

        assertTrue(taskExecuted)
    }
}
