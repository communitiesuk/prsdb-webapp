package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.JointLandlordsPropertyRegistrationStrategy

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
}
