package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE

class JoinPropertyControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @WithMockUser(roles = ["LANDLORD"])
    @Test
    fun `join property endpoints are unavailable if their feature flag is disabled`() {
        featureFlagManager.disableFeature(JOINT_LANDLORDS)

        mvc
            .get(JOIN_PROPERTY_ROUTE)
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["LANDLORD"])
    @Test
    fun `join property index endpoint is available if its feature flag is enabled`() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)

        mvc
            .get(JOIN_PROPERTY_ROUTE)
            .andExpect { status { isOk() } }
    }
}
