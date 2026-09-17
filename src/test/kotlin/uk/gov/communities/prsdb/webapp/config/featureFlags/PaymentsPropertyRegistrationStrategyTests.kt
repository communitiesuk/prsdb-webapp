package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.PAYMENTS
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PaymentsPropertyRegistrationStrategy

class PaymentsPropertyRegistrationStrategyTests : FeatureFlagTest() {
    @Autowired
    lateinit var strategy: PaymentsPropertyRegistrationStrategy

    @Test
    fun `when feature is disabled ifEnabled does not execute the action`() {
        featureFlagManager.disableFeature(PAYMENTS)
        var actionExecuted = false

        strategy.ifEnabled { actionExecuted = true }

        assertFalse(actionExecuted)
    }

    @Test
    fun `when feature is enabled ifEnabled executes the action`() {
        featureFlagManager.enableFeature(PAYMENTS)
        var actionExecuted = false

        strategy.ifEnabled { actionExecuted = true }

        assertTrue(actionExecuted)
    }

    @Test
    fun `when feature is disabled ifEnabledOrElse returns the disabled branch`() {
        featureFlagManager.disableFeature(PAYMENTS)

        assertEquals(
            "off",
            strategy.ifEnabledOrElse {
                ifEnabled { "on" }
                ifDisabled { "off" }
            },
        )
    }

    @Test
    fun `when feature is enabled ifEnabledOrElse returns the enabled branch`() {
        featureFlagManager.enableFeature(PAYMENTS)

        assertEquals(
            "on",
            strategy.ifEnabledOrElse {
                ifEnabled { "on" }
                ifDisabled { "off" }
            },
        )
    }
}
