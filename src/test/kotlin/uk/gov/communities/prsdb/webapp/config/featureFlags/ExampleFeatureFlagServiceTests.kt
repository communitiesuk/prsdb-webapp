package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.services.interfaces.ExampleFeatureFlaggedService

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
class ExampleFeatureFlagServiceTests : FeatureFlagTest() {
    @Autowired
    lateinit var service: ExampleFeatureFlaggedService

    @Test
    fun `when feature is disabled service uses flag-off bean`() {
        featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_ONE)
        assertEquals("Using ExampleFeatureFlaggedService - Flag OFF", service.getFeatureFlagPageHeading())
    }

    @Test
    fun `when feature is disabled service uses flag-on bean`() {
        featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_ONE)
        assertEquals("Using ExampleFeatureFlaggedService - Flag ON", service.getFeatureFlagPageHeading())
    }

    @Test
    fun `getTemplateName returns disabledFeature when feature is disabled`() {
        featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_ONE)
        assertEquals("featureFlagExamples/disabledFeature", service.getTemplateName())
    }

    @Test
    fun `getTemplateName returns enabledFeature when feature is enabled`() {
        featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_ONE)
        assertEquals("featureFlagExamples/enabledFeature", service.getTemplateName())
    }
}
