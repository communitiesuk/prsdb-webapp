package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_GROUPED_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_GROUPED_FEATURE_FLAG_TWO
import uk.gov.communities.prsdb.webapp.constants.RELEASE_1_0
import uk.gov.communities.prsdb.webapp.constants.RELEASE_2_0
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURE_FLAGGED_GROUPED_ENDPOINT_GROUPED_FLAG_ONE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURE_FLAGGED_GROUPED_ENDPOINT_GROUPED_FLAG_TWO_ROUTE

class ExampleGroupedFeatureFlaggedEndpointAvailabilityTests : FeatureFlagTestCallingEndpoints() {
    @Test
    @WithMockUser
    fun `groupedFeatureFlagEnabled endpoint is available when the individual flag is on and the group flag is on`() {
        featureFlagManager.enableFeature(EXAMPLE_GROUPED_FEATURE_FLAG_ONE)
        featureFlagManager.enableFeatureGroup(RELEASE_1_0)
        mvc
            .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_GROUPED_FLAG_ONE_ROUTE)
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `groupedFeatureFlagEnabled endpoint is available when the individual flag is off and the group flag is on`() {
        featureFlagManager.disableFeature(EXAMPLE_GROUPED_FEATURE_FLAG_TWO)
        featureFlagManager.enableFeatureGroup(RELEASE_1_0)
        mvc
            .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_GROUPED_FLAG_TWO_ROUTE)
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `groupedFeatureFlagEnabled endpoint is unavailable when the individual flag is on and the group flag is off`() {
        featureFlagManager.enableFeature(EXAMPLE_GROUPED_FEATURE_FLAG_ONE)
        featureFlagManager.disableFeatureGroup(RELEASE_1_0)
        mvc
            .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_GROUPED_FLAG_ONE_ROUTE)
            .andExpect { status { isNotFound() } }
    }

    @Test
    @WithMockUser
    fun `groupedFeatureFlagEnabled endpoint is unavailable when the individual flag is off and the group flag is off`() {
        featureFlagManager.disableFeature(EXAMPLE_GROUPED_FEATURE_FLAG_TWO)
        featureFlagManager.disableFeatureGroup(RELEASE_1_0)
        mvc
            .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_GROUPED_FLAG_TWO_ROUTE)
            .andExpect { status { isNotFound() } }
    }

    @Nested
    inner class WithReleaseDateFlippingStrategyTests {
        @Test
        @WithMockUser
        fun `groupedFeatureFlagEnabled endpoint is unavailable before release date even if group is enabled`() {
            featureFlagManager.enableFeatureGroup(RELEASE_2_0)
            // Set release date to future date
            val futureDate =
                java.time.LocalDate
                    .now()
                    .plusDays(10)
            featureFlagManager.addReleaseDateFlippingStrategyToFeaturesInGroup(RELEASE_2_0, futureDate)

            mvc
                .get(FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
                .andExpect { status { isNotFound() } }
        }

        @Test
        @WithMockUser
        fun `groupedFeatureFlagEnabled endpoint is available on release date if group is enabled`() {
            featureFlagManager.enableFeatureGroup(RELEASE_2_0)
            // Set release date to today
            val dateNow = java.time.LocalDate.now()
            featureFlagManager.addReleaseDateFlippingStrategyToFeaturesInGroup(RELEASE_2_0, dateNow)

            mvc
                .get(FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
                .andExpect { status { isOk() } }
        }

        @Test
        @WithMockUser
        fun `groupedFeatureFlagEnabled endpoint is available after release date if group is enabled`() {
            featureFlagManager.enableFeatureGroup(RELEASE_2_0)
            // Set release date to past date
            val pastDate =
                java.time.LocalDate
                    .now()
                    .minusDays(1)
            featureFlagManager.addReleaseDateFlippingStrategyToFeaturesInGroup(RELEASE_1_0, pastDate)

            mvc
                .get(FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
                .andExpect { status { isOk() } }
        }

        @Test
        @WithMockUser
        fun `groupedFeatureFlagEnabled endpoint is unavailable after release date even if group is disabled`() {
            featureFlagManager.disableFeatureGroup(RELEASE_2_0)
            // Set release date to future date
            val pastDate =
                java.time.LocalDate
                    .now()
                    .minusDays(1)
            featureFlagManager.addReleaseDateFlippingStrategyToFeaturesInGroup(RELEASE_2_0, pastDate)

            mvc
                .get(FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE_ROUTE)
                .andExpect { status { isNotFound() } }
        }
    }
}
