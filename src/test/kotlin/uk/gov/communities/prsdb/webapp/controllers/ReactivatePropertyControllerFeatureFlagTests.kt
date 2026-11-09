package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController.Companion.getPropertyReactivationBasePath
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController.Companion.getPropertyReactivationPath
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

// TODO PDJB-2149: Delete this class when the ACTIVE_INACTIVE flag is removed
class ReactivatePropertyControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @MockitoBean
    private lateinit var journeyFactory: PropertyReactivationJourneyFactory

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val propertyOwnershipId = 1L
    private val redirectModelAndView = ModelAndView("redirect:/next")

    @BeforeEach
    fun setUp() {
        whenever(propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, isActive = false))
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(ReactivationInfoStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
        whenever(stepLifecycleOrchestrator.getStepModelAndView()).thenReturn(redirectModelAndView)
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any())).thenReturn(redirectModelAndView)
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `reactivation journey is unavailable if the active inactive feature flag is disabled`() {
        featureFlagManager.disableFeature(ACTIVE_INACTIVE)

        mvc
            .get(getPropertyReactivationPath(propertyOwnershipId))
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `reactivation journey is available if the active inactive feature flag is enabled`() {
        featureFlagManager.enableFeature(ACTIVE_INACTIVE)

        mvc
            .get(getPropertyReactivationPath(propertyOwnershipId))
            .andExpect { status { is3xxRedirection() } }
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `submitting the reactivation journey is unavailable if the active inactive feature flag is disabled`() {
        featureFlagManager.disableFeature(ACTIVE_INACTIVE)

        mvc
            .post(getPropertyReactivationPath(propertyOwnershipId)) {
                param("formData", "")
                with(csrf())
            }.andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `submitting the reactivation journey is available if the active inactive feature flag is enabled`() {
        featureFlagManager.enableFeature(ACTIVE_INACTIVE)

        mvc
            .post(getPropertyReactivationPath(propertyOwnershipId)) {
                param("formData", "")
                with(csrf())
            }.andExpect { status { is3xxRedirection() } }
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `reactivation confirmation page is unavailable if the active inactive feature flag is disabled`() {
        featureFlagManager.disableFeature(ACTIVE_INACTIVE)

        mvc
            .get("${getPropertyReactivationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `reactivation confirmation page is available if the active inactive feature flag is enabled`() {
        featureFlagManager.enableFeature(ACTIVE_INACTIVE)

        mvc
            .get("${getPropertyReactivationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect { status { isOk() } }
    }
}
