package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(UpdateLicensingController::class)
class UpdateLicensingControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateLicensingJourneyFactory

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        UpdateLicensingController.getUpdateLicensingBaseRoute(propertyOwnershipId) +
            "/${LicensingTypeStep.ROUTE_SEGMENT}"

    override val formContent = "licensingType=NO_LICENSING"

    override fun stubJourneyStepGet() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(LicensingTypeStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    override fun stubJourneyStepPost(redirectUrl: String) {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(LicensingTypeStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}
