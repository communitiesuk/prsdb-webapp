package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
class ExampleFeatureFlaggedEndpointAvailabilityTests : FeatureFlagTest() {
    @Autowired
    lateinit var webContext: WebApplicationContext

    lateinit var mvc: MockMvc

    @BeforeEach
    fun setup() {
        mvc =
            MockMvcBuilders
                .webAppContextSetup(webContext)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()
    }

    @Test
    @WithMockUser
    fun `featureFlagEnabled endpoint is available when the flag is on`() {
        featureFlagManager.enable(EXAMPLE_FEATURE_FLAG_ONE)
        mvc
            .get(FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `featureFlagEnabled endpoint is unavailable when the flag is off`() {
        featureFlagManager.disable(EXAMPLE_FEATURE_FLAG_ONE)
        mvc
            .get(FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isNotFound() } }
    }

    @Test
    @WithMockUser
    fun `inverseFeatureFlagEnabled endpoint is available when the flag is off`() {
        featureFlagManager.disable(EXAMPLE_FEATURE_FLAG_ONE)
        mvc
            .get(INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `inverseFeatureFlagEnabled endpoint is unavailable when the flag is on`() {
        featureFlagManager.enable(EXAMPLE_FEATURE_FLAG_ONE)
        mvc
            .get(INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isNotFound() } }
    }
}
