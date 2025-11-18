package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURED_FLAGGED_TEMPLATE_TEST_URL_ROUTE

class ExampleFeatureFlaggedTemplateSelectionTests : FeatureFlagTestCallingEndpoints() {
    @Test
    @WithMockUser
    fun `when feature is enabled returns enabledFeature template`() {
        featureFlagManager.enableFeature(EXAMPLE_FEATURE_FLAG_ONE)

        mvc
            .get(FEATURED_FLAGGED_TEMPLATE_TEST_URL_ROUTE)
            .andExpect {
                view { name("enabledFeature") }
            }
    }

    @Test
    @WithMockUser
    fun `when feature is disabled returns disabledFeature template`() {
        featureFlagManager.disableFeature(EXAMPLE_FEATURE_FLAG_ONE)

        mvc
            .get(FEATURED_FLAGGED_TEMPLATE_TEST_URL_ROUTE)
            .andExpect {
                view { name("disabledFeature") }
            }
    }
}
