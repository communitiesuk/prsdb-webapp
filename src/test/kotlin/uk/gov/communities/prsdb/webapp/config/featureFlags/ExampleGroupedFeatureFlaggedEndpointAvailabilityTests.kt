package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_THREE
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_TWO
import uk.gov.communities.prsdb.webapp.constants.RELEASE_1_0
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE

class ExampleGroupedFeatureFlaggedEndpointAvailabilityTests : FeatureFlagTestCallingEndpoints() {
    @Test
    @WithMockUser
    fun `groupedFeatureFlagEnabled endpoint is available when the individual flag is on and the group flag is on`() {
        featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_TWO)
        featureFlagManager.enableFeatureRelease(RELEASE_1_0)
        mvc
            .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE)
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `groupedFeatureFlagEnabled endpoint is available when the individual flag is off and the group flag is on`() {
        featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_THREE)
        featureFlagManager.enableFeatureRelease(RELEASE_1_0)
        mvc
            .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE)
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `groupedFeatureFlagEnabled endpoint is unavailable when the individual flag is on and the group flag is off`() {
        featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_TWO)
        featureFlagManager.disableFeatureRelease(RELEASE_1_0)
        mvc
            .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_2_ROUTE)
            .andExpect { status { isNotFound() } }
    }

    @Test
    @WithMockUser
    fun `groupedFeatureFlagEnabled endpoint is unavailable when the individual flag is off and the group flag is off`() {
        featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_THREE)
        featureFlagManager.disableFeatureRelease(RELEASE_1_0)
        mvc
            .get(FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE)
            .andExpect { status { isNotFound() } }
    }
}
