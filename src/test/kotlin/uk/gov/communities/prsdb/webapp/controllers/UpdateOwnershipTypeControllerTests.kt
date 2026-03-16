package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType.UpdateOwnershipTypeJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(UpdateOwnershipTypeController::class)
class UpdateOwnershipTypeControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateOwnershipTypeJourneyFactory

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        UpdateOwnershipTypeController.getUpdateOwnershipTypeRoute(propertyOwnershipId) +
            "/${OwnershipTypeStep.ROUTE_SEGMENT}"

    override val formContent = "ownershipType=FREEHOLD"

    override fun stubJourneyStepGet() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(OwnershipTypeStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    override fun stubJourneyStepPost(redirectUrl: String) {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(OwnershipTypeStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}
