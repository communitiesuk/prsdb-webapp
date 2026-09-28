package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.GasSupplyProvideLaterStrategy

class GasSupplyProvideLaterStrategyTests : FeatureFlagTest() {
    @Autowired
    lateinit var strategy: GasSupplyProvideLaterStrategy

    @Test
    fun `when feature is disabled ifEnabledOrElse resolves the disabled branch`() {
        featureFlagManager.disableFeature(DELEGATE_TO_LETTING_AGENT)

        val result =
            strategy.ifEnabledOrElse {
                ifEnabled { "enabled" }
                ifDisabled { "disabled" }
            }

        assertEquals("disabled", result)
    }

    @Test
    fun `when feature is enabled ifEnabledOrElse resolves the enabled branch`() {
        featureFlagManager.enableFeature(DELEGATE_TO_LETTING_AGENT)

        val result =
            strategy.ifEnabledOrElse {
                ifEnabled { "enabled" }
                ifDisabled { "disabled" }
            }

        assertEquals("enabled", result)
    }
}
