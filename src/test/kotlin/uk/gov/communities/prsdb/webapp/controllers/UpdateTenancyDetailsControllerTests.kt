package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails.UpdateTenancyDetailsJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(UpdateTenancyDetailsController::class)
class UpdateTenancyDetailsControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateTenancyDetailsJourneyFactory

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        UpdateTenancyDetailsController.getUpdateTenancyDetailsRoute(propertyOwnershipId) +
            "/${HouseholdStep.ROUTE_SEGMENT}"

    override val formContent = "numberOfHouseholds=2"

    override fun stubCreateJourneySteps() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(HouseholdStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}
