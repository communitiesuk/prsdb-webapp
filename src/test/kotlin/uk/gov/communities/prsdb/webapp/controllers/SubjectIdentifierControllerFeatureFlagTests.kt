package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.SUBJECT_IDENTIFIER_PAGE
import uk.gov.communities.prsdb.webapp.controllers.SubjectIdentifierController.Companion.LOCAL_COUNCIL_SUBJECT_IDENTIFIER_URL
import uk.gov.communities.prsdb.webapp.controllers.SubjectIdentifierController.Companion.SYSTEM_OPERATOR_SUBJECT_IDENTIFIER_URL
import kotlin.test.Test

class SubjectIdentifierControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
    @Test
    fun `LC subject identifier endpoint is unavailable if feature flag is disabled`() {
        featureFlagManager.disableFeature(SUBJECT_IDENTIFIER_PAGE)

        mvc
            .get(LOCAL_COUNCIL_SUBJECT_IDENTIFIER_URL)
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
    @Test
    fun `LC subject identifier endpoint is available if feature flag is enabled`() {
        featureFlagManager.enableFeature(SUBJECT_IDENTIFIER_PAGE)

        mvc
            .get(LOCAL_COUNCIL_SUBJECT_IDENTIFIER_URL)
            .andExpect { status { isOk() } }
    }

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `system operator subject identifier endpoint is unavailable if feature flag is disabled`() {
        featureFlagManager.disableFeature(SUBJECT_IDENTIFIER_PAGE)

        mvc
            .get(SYSTEM_OPERATOR_SUBJECT_IDENTIFIER_URL)
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `system operator subject identifier endpoint is available if feature flag is enabled`() {
        featureFlagManager.enableFeature(SUBJECT_IDENTIFIER_PAGE)

        mvc
            .get(SYSTEM_OPERATOR_SUBJECT_IDENTIFIER_URL)
            .andExpect { status { isOk() } }
    }
}
