package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURED_FLAGGED_SERVICE_TEST_URL_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE
import uk.gov.communities.prsdb.webapp.services.interfaces.ExampleFeatureFlaggedService

// TODO PRSD-1683 - delete example feature flag implementation when no longer needed
@WebMvcTest(ExampleFeatureFlagTestController::class)
class ExampleFeatureFlagTestControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    // In this test, the controller endpoint is called whether the feature is enabled or not because
    // @WebMvcTest doesn't check the WebMvcRegistrations
    //
    // These tests are for checking the functionality of the controller endpoints.
    //
    // See ExampleFeatureFlaggedEndpointAvailabilityTests for tests that check the availability of the endpoints

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    @MockitoBean
    private lateinit var exampleFeatureFlaggedService: ExampleFeatureFlaggedService

    @WithMockUser
    @Test
    fun `featureFlaggedEndpointTest calls the featureFlagManager to check if the feature is enabled`() {
        mvc
            .get(FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isOk() } }

        verify(featureFlagManager).checkFeature(EXAMPLE_FEATURE_FLAG_ONE)
    }

    @WithMockUser
    @Test
    fun `inverseFeatureFlaggedEndpointTest calls the featureFlagManager to check if the feature is enabled`() {
        mvc
            .get(INVERSE_FEATURED_FLAGGED_ENDPOINT_TEST_URL_ROUTE)
            .andExpect { status { isOk() } }

        verify(featureFlagManager).checkFeature(EXAMPLE_FEATURE_FLAG_ONE)
    }

    @WithMockUser
    @Test
    fun `featureFlaggedServiceTests shows different content depending on feature flag state`() {
        // When feature flag is enabled
        whenever(featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE)).thenReturn(true)
        mvc
            .get(FEATURED_FLAGGED_SERVICE_TEST_URL_ROUTE)
            .andExpect {
                status { isOk() }
                model { attribute("ffConfigFeature", "Feature Flag in FeatureFlagConfig is ON") }
            }

        // When feature flag is disabled
        whenever(featureFlagManager.checkFeature(EXAMPLE_FEATURE_FLAG_ONE)).thenReturn(false)
        mvc
            .get(FEATURED_FLAGGED_SERVICE_TEST_URL_ROUTE)
            .andExpect {
                status { isOk() }
                model { attribute("ffConfigFeature", "Feature Flag in FeatureFlagConfig is OFF") }
            }
    }
}
