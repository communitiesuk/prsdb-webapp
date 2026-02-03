package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.RELEASE_1_0

/**
 * Integration tests to verify feature flag isolation between tests.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FeatureFlagIsolationIntegrationTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    @Order(1)
    fun `test 1 - modify feature flag state`() {
        assertTrue(featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE))
        featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_ONE)
        assertFalse(featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE))
    }

    @Test
    @Order(2)
    fun `test 2 - verify feature flag was reset`() {
        assertTrue(
            featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE),
            "Flag should be reset to default - previous test changes should not leak",
        )
    }

    @Test
    @Order(3)
    fun `test 3 - modify release state`() {
        assertFalse(featureFlagManager.getFeaturesByGroup(RELEASE_1_0).values.all { it.isEnable })
        featureFlagManager.enableFeatureRelease(RELEASE_1_0)
        assertTrue(featureFlagManager.getFeaturesByGroup(RELEASE_1_0).values.all { it.isEnable })
    }

    @Test
    @Order(4)
    fun `test 4 - verify release was reset`() {
        assertFalse(
            featureFlagManager.getFeaturesByGroup(RELEASE_1_0).values.all { it.isEnable },
            "Release should be reset to default - previous test changes should not leak",
        )
    }
}
