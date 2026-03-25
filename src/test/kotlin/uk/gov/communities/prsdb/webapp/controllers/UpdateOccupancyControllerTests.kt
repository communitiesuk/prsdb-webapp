package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
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
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        UpdateOccupancyController.getUpdateOccupancyRoute(propertyOwnershipId) +
            "/${OccupiedStep.ROUTE_SEGMENT}"

    override val formContent = "occupied=true"

    override fun stubCreateJourneySteps() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(OccupiedStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}
