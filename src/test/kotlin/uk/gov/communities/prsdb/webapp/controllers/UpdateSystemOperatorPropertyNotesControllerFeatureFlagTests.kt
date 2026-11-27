package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.ACT_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.controllers.UpdateSystemOperatorPropertyNotesController.Companion.getUpdateNotesStepPath
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractSystemOperatorNotesStep
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.propertyNotes.UpdateSystemOperatorPropertyNotesJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

// TODO PDJB-2288: Delete this class when the ACT_AS_LANDLORD flag is removed
class UpdateSystemOperatorPropertyNotesControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var journeyFactory: UpdateSystemOperatorPropertyNotesJourneyFactory

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val propertyOwnershipId = 1L

    @BeforeEach
    fun setUp() {
        whenever(propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(AbstractSystemOperatorNotesStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
        whenever(stepLifecycleOrchestrator.getStepModelAndView()).thenReturn(ModelAndView("redirect:/next"))
    }

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `system operator property notes journey is unavailable if the act as landlord feature flag is disabled`() {
        featureFlagManager.disableFeature(ACT_AS_LANDLORD)

        mvc
            .get(getUpdateNotesStepPath(propertyOwnershipId))
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    @Test
    fun `system operator property notes journey is available if the act as landlord feature flag is enabled`() {
        featureFlagManager.enableFeature(ACT_AS_LANDLORD)

        mvc
            .get(getUpdateNotesStepPath(propertyOwnershipId))
            .andExpect { status { is3xxRedirection() } }
    }
}
