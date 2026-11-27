package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.ACT_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController.Companion.getPropertyDetailsForSystemOperatorPath
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModelFactory
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData.Companion.statusDerivingService

// TODO PDJB-2288: Delete this class when the ACT_AS_LANDLORD flag is removed
class PropertyDetailsControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var jointLandlordInvitationService: JointLandlordInvitationService

    @MockitoBean
    private lateinit var propertyComplianceViewModelFactory: PropertyComplianceViewModelFactory

    private val propertyOwnershipId = 1L

    @BeforeEach
    fun setUp() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()
        val complianceStatus = statusDerivingService.getComplianceStatus(propertyCompliance)

        whenever(propertyOwnershipService.retrievePropertyOwnershipByIdRegardlessOfActiveStatus(propertyOwnershipId))
            .thenReturn(propertyOwnership)
        whenever(propertyComplianceService.getComplianceForPropertyOrNull(any())).thenReturn(propertyCompliance)
        whenever(propertyComplianceService.getComplianceStatus(any())).thenReturn(complianceStatus)
        whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
            .thenReturn(Pair(emptyList(), emptyList()))
    }

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `system operator property details page is unavailable if the act as landlord feature flag is disabled`() {
        featureFlagManager.disableFeature(ACT_AS_LANDLORD)

        mvc
            .get(getPropertyDetailsForSystemOperatorPath(propertyOwnershipId))
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `system operator property details page is available if the act as landlord feature flag is enabled`() {
        featureFlagManager.enableFeature(ACT_AS_LANDLORD)

        mvc
            .get(getPropertyDetailsForSystemOperatorPath(propertyOwnershipId))
            .andExpect { status { isOk() } }
    }
}
