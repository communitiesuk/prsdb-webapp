package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.correspondenceEmail.UpdateCorrespondenceEmailJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(LandlordUpdateCorrespondenceEmailController::class)
class LandlordUpdateCorrespondenceEmailControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateCorrespondenceEmailJourneyFactory

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val propertyOwnershipId = 1L
    override val updateStepRoute =
        LandlordUpdateCorrespondenceEmailController.getUpdateCorrespondenceEmailRoute(propertyOwnershipId) +
            "/${CorrespondenceEmailStep.ROUTE_SEGMENT}"
    override val formContent = "correspondenceEmailOption=ACCOUNT_EMAIL" // pragma: allowlist secret

    override fun stubCreateJourneySteps() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(CorrespondenceEmailStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}
