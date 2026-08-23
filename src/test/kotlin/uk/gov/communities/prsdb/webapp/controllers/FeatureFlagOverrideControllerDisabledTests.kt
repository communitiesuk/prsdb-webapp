package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.FeatureFlagOverrideController.Companion.FEATURE_FLAG_OVERRIDES_ROUTE
import uk.gov.communities.prsdb.webapp.services.FeatureFlagOverrideService

@WebMvcTest(FeatureFlagOverrideController::class)
class FeatureFlagOverrideControllerDisabledTests(
    @Autowired val context: WebApplicationContext,
) : ControllerTest(context) {
    @MockitoBean
    lateinit var featureFlagOverrideService: FeatureFlagOverrideService

    @MockitoBean
    lateinit var featureFlagManager: FeatureFlagManager

    @MockitoBean
    lateinit var featureFlagConfig: FeatureFlagConfig

    @Test
    fun `the overrides page is not served when overrides are not enabled`() {
        mvc
            .perform(get(FEATURE_FLAG_OVERRIDES_ROUTE))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    fun `the overrides route is indistinguishable from an unmapped system operator route when overrides are not enabled`() {
        val overridesResponse = mvc.perform(get(FEATURE_FLAG_OVERRIDES_ROUTE)).andReturn().response
        val unmappedResponse = mvc.perform(get("/$SYSTEM_OPERATOR_PATH_SEGMENT/an-unmapped-route")).andReturn().response

        assertEquals(unmappedResponse.status, overridesResponse.status)
        assertEquals(unmappedResponse.redirectedUrl, overridesResponse.redirectedUrl)
    }
}
