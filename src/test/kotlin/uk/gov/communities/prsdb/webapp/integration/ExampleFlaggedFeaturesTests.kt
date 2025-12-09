package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_TWO
import uk.gov.communities.prsdb.webapp.constants.RELEASE_1_0
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater
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
        @Test
        fun `feature is enabled if the release date is in the past`() {
            // Setup feature configuration with flip strategy release date in the past
            featureFlagManager.enableFeatureRelease(RELEASE_1_0)
            featureFlagConfigUpdater.updateFeatureReleaseDate(EXAMPLE_FEATURE_FLAG_TWO, LocalDate.now().minusWeeks(5))

            val featureEnabledPage = navigator.goToFeatureFlagTwoEnabledPage()

            assertThat(featureEnabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is ENABLED")
        }

        @Test
        fun `feature is disabled if the release date is in the future`() {
            // Setup feature configuration with flip strategy release date in the future
            featureFlagManager.enableFeatureRelease(RELEASE_1_0)
            featureFlagConfigUpdater.updateFeatureReleaseDate(EXAMPLE_FEATURE_FLAG_TWO, LocalDate.now().plusWeeks(5))

            val featureDisabledPage = navigator.goToFeatureFlagTwoDisabledPage()

            assertThat(featureDisabledPage.heading).containsText("Feature flagged controller endpoint - available when flag is DISABLED")
        }
    }

    @Nested
    inner class FeatureReleaseTests {
        // TODO PRSD-1647
    }

    @Nested
    inner class FeatureReleaseWithFlipStrategyTests {
        // TODO PRSD-1647 - add tests for one of the endpoints in a release with a flip strategy - updated releaseDate to past date and future date
        //    - Can we write a helper that will get in and update the release date - the CombinedFlipStrategy includes the list of all strategies
        //      so should be able to get into and do the setReleaseDate
        //    - We might actually want to be able to update the whole config so we can see the interaction? Whole config is how it will be set for real...
    }

    @Nested
    inner class MultipleFeaturesInteractionTests {
        // TODO PRSD-1647 - add tests for interaction between multiple features
        //  - e.g. feature flagged endpoint calling feature flagged service
        //  - e.g. feature flagged service calling another feature flagged service
        //  - e.g. feature flagged service calling another feature flagged service that's in a release with a flip strategy
        //
        // Do we want a helper file?
        // Do we update the whole config for this?  Probably but don't necessarily want to pass in the whole config each time, maybe a helper can update either everything or one value...
        // How - do we pass in the whole config? O
        //
        // May not actually need to add a complex example, but add an example of updating the whole config that can be used if needed.
    }
}
