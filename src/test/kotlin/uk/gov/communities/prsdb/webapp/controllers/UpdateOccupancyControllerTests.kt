package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(UpdateOccupancyController::class)
class UpdateOccupancyControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateOccupancyJourneyFactory

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        UpdateOccupancyController.getUpdateOccupancyRoute(propertyOwnershipId) +
            "/${OccupiedStep.ROUTE_SEGMENT}"

    override val formContent = "occupied=true"

    override fun stubCreateJourneySteps() {
        whenever(journeyFactory.createJourneySteps(eq(propertyOwnershipId), any()))
            .thenReturn(mapOf(OccupiedStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `getUpdateStep builds the journey with the check answers page when DELEGATE_TO_LETTING_AGENT is enabled`() {
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
        stubCreateJourneySteps()
        whenever(stepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc.get(updateStepRoute).andExpect { status { isOk() } }

        verify(journeyFactory).createJourneySteps(propertyOwnershipId, true)
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `getUpdateStep builds the single-page journey when DELEGATE_TO_LETTING_AGENT is disabled`() {
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)
        stubCreateJourneySteps()
        whenever(stepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc.get(updateStepRoute).andExpect { status { isOk() } }

        verify(journeyFactory).createJourneySteps(propertyOwnershipId, false)
    }
}
