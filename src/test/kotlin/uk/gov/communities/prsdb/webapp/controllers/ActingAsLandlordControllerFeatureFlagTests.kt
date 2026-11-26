package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.post
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.ACT_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.controllers.ActingAsLandlordController.Companion.STOP_ACTING_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ActingAsLandlordController.Companion.getActAsLandlordPath
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService

// TODO PDJB-2288: Delete this class when the ACT_AS_LANDLORD flag is removed
class ActingAsLandlordControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @MockitoBean
    private lateinit var actingAsLandlordService: ActingAsLandlordService

    private val landlordId = 1L

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `act-as is unavailable if the act as landlord feature flag is disabled`() {
        featureFlagManager.disableFeature(ACT_AS_LANDLORD)

        mvc
            .post(getActAsLandlordPath(landlordId)) { with(csrf()) }
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `act-as is available if the act as landlord feature flag is enabled`() {
        featureFlagManager.enableFeature(ACT_AS_LANDLORD)

        mvc
            .post(getActAsLandlordPath(landlordId)) { with(csrf()) }
            .andExpect { status { is3xxRedirection() } }
    }

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `stop-acting is unavailable if the act as landlord feature flag is disabled`() {
        featureFlagManager.disableFeature(ACT_AS_LANDLORD)

        mvc
            .post(STOP_ACTING_ROUTE) { with(csrf()) }
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `stop-acting is available if the act as landlord feature flag is enabled`() {
        featureFlagManager.enableFeature(ACT_AS_LANDLORD)

        mvc
            .post(STOP_ACTING_ROUTE) { with(csrf()) }
            .andExpect { status { is3xxRedirection() } }
    }
}
