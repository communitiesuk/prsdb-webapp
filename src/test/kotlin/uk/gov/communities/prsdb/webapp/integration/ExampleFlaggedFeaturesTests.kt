package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import kotlin.test.Test

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
class ExampleFlaggedFeaturesTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class IndividualFeatureTests {
        @Test
        fun `service test endpoint displays the heading returned by ExampleFeatureFlagServiceImplFlagOn when the feature is enabled`() {
            // Setup feature configuration. This flag is not part of a release, so we can directly enable/disable it.
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_ONE)

            val serviceTestPage = navigator.goToFeatureFlaggedServiceTestUrlRoute()

            assertThat(serviceTestPage.heading).containsText("Using ExampleFeatureFlaggedService - Flag ON")
        }

        @Test
        fun `service test endpoint displays the heading returned by ExampleFeatureFlagServiceImplFlagOff when the feature is disabled`() {
            // Setup feature configuration. This flag is not part of a release, so we can directly enable/disable it.
            featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_ONE)

            val serviceTestPage = navigator.goToFeatureFlaggedServiceTestUrlRoute()

            assertThat(serviceTestPage.heading).containsText("Using ExampleFeatureFlaggedService - Flag OFF")
        }
    }

    @Nested
    inner class IndividualFeatureWithFlipStrategyTests {
        // TODO PRSD-1647 - add tests for one of the endpoints with a flip strategy - enabled and disabled
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
    }
}
