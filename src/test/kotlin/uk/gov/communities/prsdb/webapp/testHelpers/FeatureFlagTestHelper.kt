package uk.gov.communities.prsdb.webapp.testHelpers

import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseConfigModel

/**
 * Test helper for resetting feature flags to a clean state.
 * This class accesses the internal FF4j featureStore to reset flags between tests.
 */
class FeatureFlagTestHelper {
    companion object {
        /**
         * Resets the feature flag manager to a specific configuration by clearing
         * the feature store and reinitializing with the provided configuration.
         *
         * This is intended for test cleanup to prevent flag leakage between tests.
         */
        fun resetToConfiguration(
            featureFlagManager: FeatureFlagManager,
            featureFlags: List<FeatureFlagConfigModel>,
            featureReleases: List<FeatureReleaseConfigModel>,
        ) {
            // Access the featureStore from the parent FF4j class
            featureFlagManager.featureStore.clear()
            featureFlagManager.initializeFeatureFlags(featureFlags)
            featureFlagManager.initialiseFeatureReleases(featureReleases)
        }
    }
}
