package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_SINGLE_FEATURE_FLAG
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
class ExampleFeatureFlaggedEndpointAvailabilityTests : FeatureFlagTestCallingEndpoints() {
    @Test
    @WithMockUser
    fun `featureFlagEnabled endpoint is available when the flag is on`() {
        featureFlagManager.enableFeature(EXAMPLE_SINGLE_FEATURE_FLAG)
        mvc
            .get(FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `featureFlagEnabled endpoint is unavailable when the flag is off`() {
        featureFlagManager.disableFeature(EXAMPLE_SINGLE_FEATURE_FLAG)
        mvc
            .get(FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isNotFound() } }
    }

    @Test
    @WithMockUser
    fun `inverseFeatureFlagEnabled endpoint is available when the flag is off`() {
        featureFlagManager.disableFeature(EXAMPLE_SINGLE_FEATURE_FLAG)
        mvc
            .get(INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `inverseFeatureFlagEnabled endpoint is unavailable when the flag is on`() {
        featureFlagManager.enableFeature(EXAMPLE_SINGLE_FEATURE_FLAG)
        mvc
            .get(INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isNotFound() } }
    }
}
