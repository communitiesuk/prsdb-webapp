package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.FAILOVER_TEST_ENDPOINTS

class FailoverTestControllerTests : FeatureFlagTestCallingEndpoints() {
    @WithMockUser
    @Test
    fun `failover endpoints are unavailable if their feature flag is disabled`() {
        featureFlagManager.disableFeature(FAILOVER_TEST_ENDPOINTS)
        val urlRoutes =
            listOf(
                FailoverTestController.ERROR_501_URL_ROUTE,
                FailoverTestController.ERROR_502_URL_ROUTE,
                FailoverTestController.ERROR_503_URL_ROUTE,
                FailoverTestController.ERROR_504_URL_ROUTE,
            )

        for (url in urlRoutes) {
            mvc
                .get(url)
                .andExpect { status { is4xxClientError() } }
        }
    }

    @WithMockUser
    @Test
    fun `failover endpoints return the correct status codes if their feature flag is enabled`() {
        featureFlagManager.enableFeature(FAILOVER_TEST_ENDPOINTS)
        val statusCodes = listOf(501, 502, 503, 504)
        val urlRoutes =
            listOf(
                FailoverTestController.ERROR_501_URL_ROUTE,
                FailoverTestController.ERROR_502_URL_ROUTE,
                FailoverTestController.ERROR_503_URL_ROUTE,
                FailoverTestController.ERROR_504_URL_ROUTE,
            )

        for (i in statusCodes.indices) {
            mvc
                .get(urlRoutes[i])
                .andExpect { status { isEqualTo(statusCodes[i]) } }
        }
    }
}
