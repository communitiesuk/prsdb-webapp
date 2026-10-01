package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.dateOfBirth.UpdateDateOfBirthJourneyFactory

@WebMvcTest(UpdateLandlordDateOfBirthController::class)
class UpdateLandlordDateOfBirthControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BaseIndividualLandlordUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateDateOfBirthJourneyFactory

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val updateStepRoute =
        UpdateLandlordDateOfBirthController.UPDATE_DATE_OF_BIRTH_ROUTE +
            "/${DateOfBirthStep.ROUTE_SEGMENT}"

    override fun stubCreateJourneySteps() {
        whenever(mockJourneyFactory.createJourneySteps())
            .thenReturn(mapOf(DateOfBirthStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}