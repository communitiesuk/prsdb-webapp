package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_FOUR
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_THREE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_TWO
import uk.gov.communities.prsdb.webapp.constants.FAILOVER_TEST_ENDPOINTS
import uk.gov.communities.prsdb.webapp.constants.RELEASE_1_0
import uk.gov.communities.prsdb.webapp.constants.RELEASE_WITH_STRATEGY
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFeatureFlagConfig
import java.time.LocalDate
import kotlin.test.Test

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
class ExampleFlaggedFeaturesTests : IntegrationTestWithImmutableData("data-local.sql") {
    private lateinit var featureFlagConfigUpdater: FeatureFlagConfigUpdater

    @BeforeAll
    fun setupFeatureFlagConfigUpdater() {
        featureFlagConfigUpdater = FeatureFlagConfigUpdater(featureFlagManager)
    }

    @Nested
    inner class IndividualFeatureTests {
        @Test
        fun `service test endpoint displays the heading returned by ExampleFeatureFlagServiceImplFlagOn when the feature is enabled`() {
            // Setup feature configuration. This flag is not part of a release, so we can directly enable/disable it.
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_ONE)

            val serviceTestPage = navigator.goToFeatureFlaggedServiceTestPage()

            assertThat(serviceTestPage.heading).containsText("Using ExampleFeatureFlaggedService - Flag ON")
        }

        @Test
        fun `service test endpoint displays the heading returned by ExampleFeatureFlagServiceImplFlagOff when the feature is disabled`() {
            // Setup feature configuration. This flag is not part of a release, so we can directly enable/disable it.
            featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_ONE)

            val serviceTestPage = navigator.goToFeatureFlaggedServiceTestPage()

            assertThat(serviceTestPage.heading).containsText("Using ExampleFeatureFlaggedService - Flag OFF")
        }
    }

    @Nested
    inner class IndividualFeatureWithFlipStrategyTests {
        // EXAMPLE_FEATURE_FLAG_TWO is part of RELEASE_1_0, so we need to enable the release first
        // RELEASE_1_0 has no other strategy configured, so the feature-specific flip strategy will be used
        @BeforeEach
        fun resetFeatureFlagConfigToEnableFeature() {
            featureFlagManager.enableFeatureRelease(RELEASE_1_0)
            featureFlagConfigUpdater.updateFeatureReleaseDate(
                EXAMPLE_FEATURE_FLAG_TWO,
                LocalDate.now().minusWeeks(5),
            )
            featureFlagConfigUpdater.updateFeatureEnabledByStrategy(
                EXAMPLE_FEATURE_FLAG_TWO,
                true,
            )
        }

        @Test
        fun `feature is enabled if the release date is in the past`() {
            // Setup feature configuration with flip strategy release date in the past
            featureFlagConfigUpdater.updateFeatureReleaseDate(
                EXAMPLE_FEATURE_FLAG_TWO,
                LocalDate.now().minusWeeks(5),
            )

            val featureEnabledPage = navigator.goToFeatureFlagTwoEnabledPage()

            assertThat(featureEnabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is ENABLED")
        }

        @Test
        fun `feature is disabled if the release date is in the future`() {
            // Setup feature configuration with flip strategy release date in the future
            featureFlagConfigUpdater.updateFeatureReleaseDate(
                EXAMPLE_FEATURE_FLAG_TWO,
                LocalDate.now().plusWeeks(5),
            )

            val featureDisabledPage = navigator.goToFeatureFlagTwoDisabledPage()

            assertThat(featureDisabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is DISABLED")
        }

        @Test
        fun `feature is enabled if enabled by the flip strategy`() {
            featureFlagConfigUpdater.updateFeatureEnabledByStrategy(
                EXAMPLE_FEATURE_FLAG_TWO,
                true,
            )

            val featureEnabledPage = navigator.goToFeatureFlagTwoEnabledPage()

            assertThat(featureEnabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is ENABLED")
        }

        @Test
        fun `feature is disabled if disabled by the flip strategy`() {
            featureFlagConfigUpdater.updateFeatureEnabledByStrategy(
                EXAMPLE_FEATURE_FLAG_TWO,
                false,
            )

            val featureDisabledPage = navigator.goToFeatureFlagTwoDisabledPage()

            assertThat(featureDisabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is DISABLED")
        }
    }

    @Nested
    inner class FeatureReleaseTests {
        // These endpoints have no strategy configured, either for their release or specifically for the feature
        @Test
        fun `feature in enabled release is enabled`() {
            featureFlagManager.enableFeatureRelease(RELEASE_1_0)

            val featureEnabledPage = navigator.goToFeatureFlagThreeEnabledPage()

            assertThat(featureEnabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is ENABLED")
        }

        @Test
        fun `feature in disabled release is disabled`() {
            featureFlagManager.disableFeatureRelease(RELEASE_1_0)

            val featureDisabledPage = navigator.goToFeatureFlagThreeDisabledPage()

            assertThat(featureDisabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is DISABLED")
        }
    }

    @Nested
    inner class FeatureReleaseWithFlipStrategyTests {
        @BeforeEach
        fun resetFeatureFlagConfigToEnableRelease() {
            featureFlagManager.enableFeatureRelease(RELEASE_WITH_STRATEGY)
            featureFlagConfigUpdater.updateReleaseReleaseDate(
                RELEASE_WITH_STRATEGY,
                LocalDate.now().minusWeeks(5),
            )
            featureFlagConfigUpdater.updateReleaseEnabledByStrategy(
                RELEASE_WITH_STRATEGY,
                true,
            )
        }

        @Test
        fun `feature in release is enabled if the release date is in the past`() {
            // Setup release configuration with flip strategy release date in the past
            featureFlagConfigUpdater.updateReleaseReleaseDate(
                RELEASE_WITH_STRATEGY,
                LocalDate.now().minusWeeks(5),
            )

            val featureEnabledPage = navigator.goToFeatureFlagFourEnabledPage()

            assertThat(featureEnabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is ENABLED")
        }

        @Test
        fun `feature in release is disabled if the release date is in the future`() {
            // Setup release configuration with flip strategy release date in the future
            featureFlagConfigUpdater.updateReleaseReleaseDate(
                RELEASE_WITH_STRATEGY,
                LocalDate.now().plusWeeks(5),
            )

            val featureDisabledPage = navigator.goToFeatureFlagFourDisabledPage()

            assertThat(featureDisabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is DISABLED")
        }

        @Test
        fun `feature in release is enabled if enabled by the flip strategy`() {
            featureFlagConfigUpdater.updateReleaseEnabledByStrategy(
                RELEASE_WITH_STRATEGY,
                true,
            )

            val featureEnabledPage = navigator.goToFeatureFlagFourEnabledPage()

            assertThat(featureEnabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is ENABLED")
        }

        @Test
        fun `feature in release is disabled if disabled by the flip strategy`() {
            featureFlagConfigUpdater.updateReleaseEnabledByStrategy(
                RELEASE_WITH_STRATEGY,
                false,
            )

            val featureDisabledPage = navigator.goToFeatureFlagFourDisabledPage()

            assertThat(featureDisabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is DISABLED")
        }
    }

    @Nested
    inner class ReinitialiseConfigTests {
        // If making several changes to the feature flag configuration compared to the default loaded from application.yml, it may be easier to
        // reinitialise the whole config.
        @BeforeEach
        fun reinitialiseFeatureFlagConfig() {
            val featureFlags =
                listOf(
                    MockFeatureFlagConfig.createFeatureFlagConfigModel(name = EXAMPLE_FEATURE_FLAG_ONE),
                    MockFeatureFlagConfig.createFeatureFlagConfigModel(name = EXAMPLE_FEATURE_FLAG_TWO, release = RELEASE_1_0),
                    MockFeatureFlagConfig.createFeatureFlagConfigModel(name = EXAMPLE_FEATURE_FLAG_THREE, release = RELEASE_1_0),
                    MockFeatureFlagConfig.createFeatureFlagConfigModel(name = EXAMPLE_FEATURE_FLAG_FOUR, release = RELEASE_WITH_STRATEGY),
                    MockFeatureFlagConfig.createFeatureFlagConfigModel(name = FAILOVER_TEST_ENDPOINTS),
                )

            val featureReleases =
                listOf(
                    MockFeatureFlagConfig.createFeatureReleaseConfigModel(name = RELEASE_1_0, enabled = true),
                    MockFeatureFlagConfig.createFeatureReleaseConfigModel(
                        name = RELEASE_WITH_STRATEGY,
                        enabled = true,
                        strategyConfig =
                            MockFeatureFlagConfig.createFlipStrategyConfigModel(
                                releaseDate = LocalDate.now().plusWeeks(2),
                            ),
                    ),
                )

            featureFlagConfigUpdater.reinitialiseFeatures(featureFlags, featureReleases)
        }

        @Test
        fun `Features in release-1-0 are enabled`() {
            val featureTwoEnabledPage = navigator.goToFeatureFlagTwoEnabledPage()
            assertThat(featureTwoEnabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is ENABLED")

            val featureThreeEnabledPage = navigator.goToFeatureFlagThreeEnabledPage()
            assertThat(featureThreeEnabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is ENABLED")
        }

        @Test
        fun `Feature in release-with-strategy is disabled due to future release date`() {
            val featureFourDisabledPage = navigator.goToFeatureFlagFourDisabledPage()
            assertThat(featureFourDisabledPage.heading)
                .containsText("Feature flagged controller endpoint - available when flag is DISABLED")
        }
    }
}
