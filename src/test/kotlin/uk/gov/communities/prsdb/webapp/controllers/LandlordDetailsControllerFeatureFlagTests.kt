package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.ACT_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.getLandlordDetailsForSystemOperatorPath
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

// TODO PDJB-2288: Delete this class when the ACT_AS_LANDLORD flag is removed
class LandlordDetailsControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    private val landlordId = 1L

    @BeforeEach
    fun setUp() {
        whenever(landlordService.retrieveLandlordById(landlordId)).thenReturn(MockLandlordData.createIndividualLandlord())
    }

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `system operator landlord details page is unavailable if the act as landlord feature flag is disabled`() {
        featureFlagManager.disableFeature(ACT_AS_LANDLORD)

        mvc
            .get(getLandlordDetailsForSystemOperatorPath(landlordId))
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `system operator landlord details page is available if the act as landlord feature flag is enabled`() {
        featureFlagManager.enableFeature(ACT_AS_LANDLORD)

        mvc
            .get(getLandlordDetailsForSystemOperatorPath(landlordId))
            .andExpect { status { isOk() } }
    }
}
