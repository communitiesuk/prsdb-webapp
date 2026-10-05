package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.phoneNumber.UpdatePhoneNumberJourneyFactory

@WebMvcTest(UpdateLandlordPhoneNumberController::class)
class UpdateLandlordPhoneNumberControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BaseIndividualLandlordUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdatePhoneNumberJourneyFactory

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val updateStepRoute =
        UpdateLandlordPhoneNumberController.UPDATE_PHONE_NUMBER_ROUTE +
            "/${PhoneNumberStep.ROUTE_SEGMENT}"

    override fun stubCreateJourneySteps() {
        whenever(mockJourneyFactory.createJourneySteps())
            .thenReturn(mapOf(PhoneNumberStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}
