package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.GATLING_POST_ENDPOINT

class PerformanceTestFormSubmissionControllerTests : FeatureFlagTestCallingEndpoints() {
    @Test
    fun `GET returns 4xx when the feature flag is disabled`() {
        featureFlagManager.disableFeature(GATLING_POST_ENDPOINT)

        mvc
            .get(PerformanceTestFormSubmissionController.PERFORMANCE_TEST_FORM_SUBMISSION_ROUTE)
            .andExpect { status { is4xxClientError() } }
    }

    @Test
    fun `POST returns 4xx when the feature flag is disabled`() {
        featureFlagManager.disableFeature(GATLING_POST_ENDPOINT)

        mvc
            .post(PerformanceTestFormSubmissionController.PERFORMANCE_TEST_FORM_SUBMISSION_ROUTE)
            .andExpect { status { is4xxClientError() } }
    }

    @Test
    fun `GET renders a page containing a CSRF token for an unauthenticated user when the feature flag is enabled`() {
        featureFlagManager.enableFeature(GATLING_POST_ENDPOINT)

        mvc
            .get(PerformanceTestFormSubmissionController.PERFORMANCE_TEST_FORM_SUBMISSION_ROUTE)
            .andExpect {
                status { isOk() }
                content {
                    contentTypeCompatibleWith("text/html")
                    string(containsString("name=\"_csrf\""))
                }
            }
    }

    @Test
    fun `POST without a CSRF token is rejected when the feature flag is enabled`() {
        featureFlagManager.enableFeature(GATLING_POST_ENDPOINT)

        mvc
            .post(PerformanceTestFormSubmissionController.PERFORMANCE_TEST_FORM_SUBMISSION_ROUTE)
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `POST with a valid CSRF token succeeds when the feature flag is enabled`() {
        featureFlagManager.enableFeature(GATLING_POST_ENDPOINT)

        val getResult =
            mvc
                .get(PerformanceTestFormSubmissionController.PERFORMANCE_TEST_FORM_SUBMISSION_ROUTE)
                .andReturn()
        val csrfTokenValue =
            Regex("name=\"_csrf\" value=\"([^\"]+)\"")
                .find(getResult.response.contentAsString)
                ?.groupValues
                ?.get(1)
                ?: error("CSRF token not found in GET response")

        mvc
            .post(PerformanceTestFormSubmissionController.PERFORMANCE_TEST_FORM_SUBMISSION_ROUTE) {
                param("_csrf", csrfTokenValue)
                session = getResult.request.session as MockHttpSession
            }.andExpect { status { isOk() } }
    }
}
