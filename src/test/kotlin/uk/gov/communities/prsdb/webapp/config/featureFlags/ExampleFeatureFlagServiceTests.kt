package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.services.interfaces.ExampleFeatureFlaggedService

class ExampleFeatureFlagServiceTests : FeatureFlagTest() {
    @Autowired
    lateinit var service: ExampleFeatureFlaggedService

    @Test
    fun `when feature is disabled service uses flag-off bean`() {
        ff4j.disable(EXAMPLE_FEATURE_FLAG_ONE)
        assertEquals("Using ExampleFeatureFlaggedService - Flag OFF", service.getFeatureFlagPageHeading())
    }

    @Test
    fun `when feature is disabled service uses flag-on bean`() {
        ff4j.enable(EXAMPLE_FEATURE_FLAG_ONE)
        assertEquals("Using ExampleFeatureFlaggedService - Flag ON", service.getFeatureFlagPageHeading())
    }
}
