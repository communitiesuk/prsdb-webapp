package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationStructureStrategy

class PropertyRegistrationStructureStrategyTests : FeatureFlagTest() {
    @Autowired
    lateinit var strategy: PropertyRegistrationStructureStrategy

    @Test
    fun `when feature is disabled ifEnabledOrElse returns the ifDisabled result`() {
        featureFlagManager.disableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)

        val result =
            strategy.ifEnabledOrElse {
                ifEnabled { "enabled" }
                ifDisabled { "disabled" }
            }

        assertTrue(result == "disabled")
    }

    @Test
    fun `when feature is enabled ifEnabledOrElse returns the ifEnabled result`() {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)

        val result =
            strategy.ifEnabledOrElse {
                ifEnabled { "enabled" }
                ifDisabled { "disabled" }
            }

        assertTrue(result == "enabled")
    }
}
