package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagOverrides
import uk.gov.communities.prsdb.webapp.services.FeatureFlagOverrideService

class FeatureFlagOverrideDefaultTests : FeatureFlagTest() {
    @Autowired
    lateinit var featureFlagOverrideService: FeatureFlagOverrideService

    // A request context is required, otherwise the service is inert for that reason instead and the tests prove nothing
    @BeforeEach
    fun setUpRequestContext() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
    }

    @AfterEach
    fun tearDownRequestContext() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `setting overrides does nothing when the enabling property is not configured`() {
        featureFlagOverrideService.setOverrides(FeatureFlagOverrides(flags = mapOf("a-flag" to true)))

        assertTrue(featureFlagOverrideService.getOverrides().isEmpty())
        assertFalse(featureFlagOverrideService.hasActiveOverrides())
    }

    @Test
    fun `flags keep their configured values when the enabling property is not configured`() {
        val flagName = featureFlagConfig.featureFlags.first().name
        val configuredValue = featureFlagManager.checkFeature(flagName)

        featureFlagOverrideService.setOverrides(FeatureFlagOverrides(flags = mapOf(flagName to !configuredValue)))

        assertTrue(featureFlagManager.checkFeature(flagName) == configuredValue)
    }
}
