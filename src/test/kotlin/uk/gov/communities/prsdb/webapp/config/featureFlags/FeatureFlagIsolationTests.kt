package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.RELEASE_1_0

/**
 * Tests to verify that feature flag changes in one test do not leak into subsequent tests.
 * These tests are ordered to ensure they run in a predictable sequence to verify isolation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FeatureFlagIsolationTests : FeatureFlagTest() {
    @Test
    @Order(1)
    fun `test 1 - enable feature flag one`() {
        // This test enables a flag
        featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_ONE)
        assertTrue(featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE))
    }

    @Test
    @Order(2)
    fun `test 2 - flag should be reset to default (enabled)`() {
        // Verify the flag is back to its default state from application.yml
        // EXAMPLE_FEATURE_FLAG_ONE is enabled by default in test application.yml
        assertTrue(
            featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE),
            "Flag should be at default state (enabled) - previous test's enableFeature should not leak",
        )
    }

    @Test
    @Order(3)
    fun `test 3 - disable feature flag one`() {
        // This test disables a flag
        featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_ONE)
        assertFalse(featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE))
    }

    @Test
    @Order(4)
    fun `test 4 - flag should be reset to default (enabled) again`() {
        // Verify the flag is back to its default state
        assertTrue(
            featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE),
            "Flag should be at default state (enabled) - previous test's disableFeature should not leak",
        )
    }

    @Test
    @Order(5)
    fun `test 5 - disable release`() {
        // RELEASE_1_0 is disabled by default in test application.yml
        // First verify default state
        assertFalse(
            featureFlagManager.getFeaturesByGroup(RELEASE_1_0).values.all { it.isEnable },
            "Release should be disabled by default",
        )

        // Enable it
        featureFlagManager.enableFeatureRelease(RELEASE_1_0)
        assertTrue(
            featureFlagManager.getFeaturesByGroup(RELEASE_1_0).values.all { it.isEnable },
            "Release should now be enabled",
        )
    }

    @Test
    @Order(6)
    fun `test 6 - release should be reset to default (disabled)`() {
        // Verify the release is back to its default state
        assertFalse(
            featureFlagManager.getFeaturesByGroup(RELEASE_1_0).values.all { it.isEnable },
            "Release should be at default state (disabled) - previous test's enableFeatureRelease should not leak",
        )
    }

    @Test
    @Order(7)
    fun `test 7 - verify feature count remains consistent`() {
        // Count should match the number of features defined in test application.yml
        val expectedFeatureCount = featureFlagConfig.featureFlags.size
        val actualFeatureCount = featureFlagManager.features.size

        assertEquals(
            expectedFeatureCount,
            actualFeatureCount,
            "Feature count should remain consistent - no features should be added or removed between tests",
        )
    }
}
