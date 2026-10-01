package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE

class TeamMembersControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @WithMockUser(roles = ["LANDLORD"])
    @Test
    fun `team members page is unavailable if the multi-user organisations feature flag is disabled`() {
        featureFlagManager.disableFeature(MULTI_USER_ORGANISATIONS)

        mvc
            .get(TEAM_MEMBERS_ROUTE)
            .andExpect { status { isNotFound() } }
    }
}
