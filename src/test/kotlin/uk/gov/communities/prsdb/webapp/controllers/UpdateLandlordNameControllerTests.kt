package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.name.UpdateNameJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep

@WebMvcTest(UpdateLandlordNameController::class)
class UpdateLandlordNameControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BaseIndividualLandlordUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateNameJourneyFactory

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val updateStepRoute =
        UpdateLandlordNameController.UPDATE_NAME_ROUTE +
            "/${NameStep.ROUTE_SEGMENT}"

    override fun stubCreateJourneySteps() {
        whenever(mockJourneyFactory.createJourneySteps())
            .thenReturn(mapOf(NameStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}
