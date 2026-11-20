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
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership.UpdateOwnershipJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

// TODO PDJB-2016: Delete this class when the ASSOCIATED_PARTIES flag is removed
class UpdateOwnershipControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @MockitoBean
    private lateinit var journeyFactory: UpdateOwnershipJourneyFactory

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val propertyOwnershipId = 1L
    private val updateStepRoute =
        UpdateOwnershipController.getUpdateOwnershipRoute(propertyOwnershipId) + "/${OwnsPropertyStep.ROUTE_SEGMENT}"
    private val redirectModelAndView = ModelAndView("redirect:/next")

    @BeforeEach
    fun setUp() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(OwnsPropertyStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
        whenever(stepLifecycleOrchestrator.getStepModelAndView()).thenReturn(redirectModelAndView)
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any())).thenReturn(redirectModelAndView)
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `ownership update journey is unavailable if the associated parties feature flag is disabled`() {
        featureFlagManager.disableFeature(ASSOCIATED_PARTIES)

        mvc
            .get(updateStepRoute)
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `ownership update journey is available if the associated parties feature flag is enabled`() {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)

        mvc
            .get(updateStepRoute)
            .andExpect { status { is3xxRedirection() } }
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `submitting the ownership update journey is unavailable if the associated parties feature flag is disabled`() {
        featureFlagManager.disableFeature(ASSOCIATED_PARTIES)

        mvc
            .post(updateStepRoute) {
                param("ownsProperty", "true")
                with(csrf())
            }.andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    @Test
    fun `submitting the ownership update journey is available if the associated parties feature flag is enabled`() {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)

        mvc
            .post(updateStepRoute) {
                param("ownsProperty", "true")
                with(csrf())
            }.andExpect { status { is3xxRedirection() } }
    }
}
