package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.address.UpdateAddressJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep

@WebMvcTest(UpdateLandlordAddressController::class)
class UpdateLandlordAddressControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BaseIndividualLandlordUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateAddressJourneyFactory

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val updateStepRoute =
        UpdateLandlordAddressController.UPDATE_ADDRESS_ROUTE +
            "/${LookupAddressStep.ROUTE_SEGMENT}"

    override fun stubCreateJourneySteps() {
        whenever(mockJourneyFactory.createJourneySteps())
            .thenReturn(mapOf(LookupAddressStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}
