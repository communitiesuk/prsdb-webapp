package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager.UpdatePropertyManagerJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

// TODO PDJB-2016: Delete this class when the ASSOCIATED_PARTIES flag is removed
class UpdatePropertyManagerControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @MockitoBean
    private lateinit var journeyFactory: UpdatePropertyManagerJourneyFactory

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val propertyOwnershipId = 1L

    private val updateStepRoute =
        UpdatePropertyManagerController.getUpdatePropertyManagerRoute(propertyOwnershipId) +
            "/${PropertyManagerStep.ROUTE_SEGMENT}"

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `getUpdateStep is unavailable if the associated parties feature flag is disabled`() {
        featureFlagManager.disableFeature(ASSOCIATED_PARTIES)

        mvc
            .get(updateStepRoute)
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `postUpdateStep is unavailable if the associated parties feature flag is disabled`() {
        featureFlagManager.disableFeature(ASSOCIATED_PARTIES)

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = "usingPropertyManager=true"
                with(csrf())
            }.andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `getUpdateStep is available if the associated parties feature flag is enabled`() {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(PropertyManagerStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
        whenever(stepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("redirect:/landlord/property-details/$propertyOwnershipId"))

        mvc
            .get(updateStepRoute)
            .andExpect { status { is3xxRedirection() } }
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `postUpdateStep is available if the associated parties feature flag is enabled`() {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(PropertyManagerStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:/landlord/property-details/$propertyOwnershipId"))

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = "usingPropertyManager=true"
                with(csrf())
            }.andExpect { status { is3xxRedirection() } }
    }
}
