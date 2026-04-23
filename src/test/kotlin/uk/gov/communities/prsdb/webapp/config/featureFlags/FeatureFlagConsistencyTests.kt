package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig
import uk.gov.communities.prsdb.webapp.constants.featureFlagNames
import kotlin.test.assertEquals

class FeatureFlagConsistencyTests : FeatureFlagTest() {
    private fun assertFeatureFlagNamesMatchConstants(config: FeatureFlagConfig) {
        val loadedFlagNames = config.featureFlags.map { it.name }.toSet()
        assertEquals(
            featureFlagNames.toSet(),
            loadedFlagNames,
            "Loaded feature flag names do not match featureFlagNames constants",
        )
    }

    @Test
    fun `default profile has feature flags matching featureFlagNames constants`() {
        assertFeatureFlagNamesMatchConstants(featureFlagConfig)
    }

    @ActiveProfiles("local")
    @Nested
    inner class LocalProfileTests : FeatureFlagTest() {
        @Test
        fun `local profile has feature flags matching featureFlagNames constants`() {
            assertFeatureFlagNamesMatchConstants(featureFlagConfig)
        }
    }

    @ActiveProfiles("integration")
    @Nested
    inner class IntegrationProfileTests : FeatureFlagTest() {
        @Test
        fun `integration profile has feature flags matching featureFlagNames constants`() {
            assertFeatureFlagNamesMatchConstants(featureFlagConfig)
        }
    }

    @ActiveProfiles("test")
    @Nested
    inner class TestProfileTests : FeatureFlagTest() {
        @Test
        fun `test profile has feature flags matching featureFlagNames constants`() {
            assertFeatureFlagNamesMatchConstants(featureFlagConfig)
        }
    }

    @ActiveProfiles("nft")
    @Nested
    inner class NftProfileTests : FeatureFlagTest() {
        @Test
        fun `nft profile has feature flags matching featureFlagNames constants`() {
            assertFeatureFlagNamesMatchConstants(featureFlagConfig)
        }
    }
}
