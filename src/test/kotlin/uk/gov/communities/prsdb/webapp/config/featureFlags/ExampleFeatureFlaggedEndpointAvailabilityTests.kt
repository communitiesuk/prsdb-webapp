package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_FOUR
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_THREE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_TWO
import uk.gov.communities.prsdb.webapp.constants.RELEASE_1_0
import uk.gov.communities.prsdb.webapp.constants.RELEASE_WITH_STRATEGY
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.INVERSE_FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.INVERSE_FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.INVERSE_FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFeatureFlagConfig
import java.time.LocalDate

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
class ExampleFeatureFlaggedEndpointAvailabilityTests : FeatureFlagTestCallingEndpoints() {
    @Test
    @WithMockUser
    fun `featureFlaggedEndpointTest endpoint is available when the flag is on`() {
        featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_ONE)
        mvc
            .get(FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `featureFlaggedEndpointTest endpoint is unavailable when the flag is off`() {
        featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_ONE)
        mvc
            .get(FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isNotFound() } }
    }

    @Test
    @WithMockUser
    fun `inverseFeatureFlaggedEndpointTest endpoint is available when the flag is off`() {
        featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_ONE)
        mvc
            .get(INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `inverseFeatureFlaggedEndpointTest endpoint is unavailable when the flag is on`() {
        featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_ONE)
        mvc
            .get(INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isNotFound() } }
    }

    @Nested
    inner class ReleaseDemoEndpoints {
        @Test
        @WithMockUser
        fun `featureReleaseFlaggedEndpointFlagTwo is available when the flag and release are enabled`() {
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_TWO)
            featureFlagManager.enableFeatureRelease(RELEASE_1_0)

            mvc
                .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE)
                .andExpect { status { isOk() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseFlaggedEndpointFlagTwo is unavailable when the flag is enabled but the release is disabled`() {
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_TWO)
            featureFlagManager.disableFeatureRelease(RELEASE_1_0)

            mvc
                .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE)
                .andExpect { status { isNotFound() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseFlaggedEndpointFlagThree is available when the flag is disabled but the release is enabled`() {
            featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_THREE)
            featureFlagManager.enableFeatureRelease(RELEASE_1_0)

            mvc
                .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE)
                .andExpect { status { isOk() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseFlaggedEndpointFlagThree is unavailable when the flag and release are disabled`() {
            featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_THREE)
            featureFlagManager.disableFeatureRelease(RELEASE_1_0)

            mvc
                .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE)
                .andExpect { status { isNotFound() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseInverseFlaggedEndpointFlagTwo is unavailable when the flag and release are enabled`() {
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_TWO)
            featureFlagManager.enableFeatureRelease(RELEASE_1_0)

            mvc
                .get(INVERSE_FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE)
                .andExpect { status { isNotFound() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseInverseFlaggedEndpointFlagTwo is available when the flag is enabled but the release is disabled`() {
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_TWO)
            featureFlagManager.disableFeatureRelease(RELEASE_1_0)

            mvc
                .get(INVERSE_FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE)
                .andExpect { status { isOk() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseInverseFlaggedEndpointFlagThree is unavailable when the flag is disabled but the release is enabled`() {
            featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_THREE)
            featureFlagManager.enableFeatureRelease(RELEASE_1_0)

            mvc
                .get(INVERSE_FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE)
                .andExpect { status { isNotFound() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseInverseFlaggedEndpointFlagThree is available when the flag and release are disabled`() {
            featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_THREE)
            featureFlagManager.disableFeatureRelease(RELEASE_1_0)

            mvc
                .get(INVERSE_FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE)
                .andExpect { status { isOk() } }
        }
    }

    @Nested
    inner class ReleaseDateFlipStrategyDemoEndpoints {
        @Test
        @WithMockUser
        fun `featureReleaseStrategyFlaggedEndpointFour is available when the current date is after the release date`() {
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_FOUR)
            initialiseReleaseWithReleaseDateStrategy(releaseDate = LocalDate.now().minusWeeks(5))

            mvc
                .get(FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
                .andExpect { status { isOk() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseStrategyFlaggedEndpointFour is available when the current date is the same as the release date`() {
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_FOUR)
            initialiseReleaseWithReleaseDateStrategy(releaseDate = LocalDate.now())

            mvc
                .get(FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
                .andExpect { status { isOk() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseStrategyFlaggedEndpointFour is unavailable when the current date is before the release date`() {
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_FOUR)
            initialiseReleaseWithReleaseDateStrategy(releaseDate = LocalDate.now().plusWeeks(5))

            mvc
                .get(FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
                .andExpect { status { isNotFound() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseStrategyInverseFlaggedEndpointFour is unavailable when the current date is after the release date`() {
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_FOUR)
            initialiseReleaseWithReleaseDateStrategy(releaseDate = LocalDate.now().minusWeeks(5))

            mvc
                .get(INVERSE_FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
                .andExpect { status { isNotFound() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseStrategyInverseFlaggedEndpointFour is unavailable when the current date is the same as the release date`() {
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_FOUR)
            initialiseReleaseWithReleaseDateStrategy(releaseDate = LocalDate.now())

            mvc
                .get(INVERSE_FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
                .andExpect { status { isNotFound() } }
        }

        @Test
        @WithMockUser
        fun `featureReleaseStrategyInverseFlaggedEndpointFour is available when the current date is before the release date`() {
            featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_FOUR)
            initialiseReleaseWithReleaseDateStrategy(releaseDate = LocalDate.now().plusWeeks(5))
            mvc
                .get(INVERSE_FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
                .andExpect { status { isOk() } }
        }

        private fun initialiseReleaseWithReleaseDateStrategy(
            releaseName: String = RELEASE_WITH_STRATEGY,
            releaseDate: LocalDate,
        ) {
            val featureReleaseConfigModel =
                MockFeatureFlagConfig.createFeatureReleaseConfigModel(
                    name = releaseName,
                    enabled = true,
                    strategyConfig = MockFeatureFlagConfig.createFlipStrategyConfigModel(releaseDate = releaseDate),
                )

            featureFlagManager.initialiseFeatureReleases(listOf(featureReleaseConfigModel))
        }
    }
}
