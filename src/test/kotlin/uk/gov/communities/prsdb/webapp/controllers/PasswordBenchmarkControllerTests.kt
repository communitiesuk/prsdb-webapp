package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.PASSWORD_BENCHMARK_ENDPOINT

class PasswordBenchmarkControllerTests : FeatureFlagTestCallingEndpoints() {
    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `endpoint returns 4xx when the feature flag is disabled`() {
        featureFlagManager.disableFeature(PASSWORD_BENCHMARK_ENDPOINT)

        mvc
            .get(PasswordBenchmarkController.PASSWORD_BENCHMARK_ROUTE)
            .andExpect { status { is4xxClientError() } }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `endpoint returns 403 for a non system operator when the feature flag is enabled`() {
        featureFlagManager.enableFeature(PASSWORD_BENCHMARK_ENDPOINT)

        mvc
            .get(PasswordBenchmarkController.PASSWORD_BENCHMARK_ROUTE)
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `endpoint returns 3xx redirect for an unauthenticated user when the feature flag is enabled`() {
        featureFlagManager.enableFeature(PASSWORD_BENCHMARK_ENDPOINT)

        mvc
            .get(PasswordBenchmarkController.PASSWORD_BENCHMARK_ROUTE)
            .andExpect { status { is3xxRedirection() } }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `endpoint returns benchmark statistics for a system operator when the feature flag is enabled`() {
        featureFlagManager.enableFeature(PASSWORD_BENCHMARK_ENDPOINT)

        mvc
            .get(PasswordBenchmarkController.PASSWORD_BENCHMARK_ROUTE)
            .andExpect {
                status { isOk() }
                content {
                    contentTypeCompatibleWith("text/plain")
                    string(containsString("50 hashes"))
                    string(containsString("iterations"))
                    string(containsString("total"))
                    string(containsString("average"))
                    string(containsString("min"))
                    string(containsString("max"))
                }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `endpoint respects overrides supplied as query parameters`() {
        featureFlagManager.enableFeature(PASSWORD_BENCHMARK_ENDPOINT)

        mvc
            .get(PasswordBenchmarkController.PASSWORD_BENCHMARK_ROUTE) {
                param("iterations", "3")
                param("memory", "8192")
                param("parallelism", "2")
                param("hashes", "5")
            }.andExpect {
                status { isOk() }
                content {
                    contentTypeCompatibleWith("text/plain")
                    string(containsString("5 hashes"))
                    string(containsString("iterations=3"))
                    string(containsString("memory=8192"))
                    string(containsString("parallelism=2"))
                    string(containsString("reproduce with ?iterations=3&memory=8192&parallelism=2&hashes=5"))
                }
            }
    }
}
