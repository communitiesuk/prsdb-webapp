package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.dateOfBirth.UpdateDateOfBirthJourneyFactory

@WebMvcTest(UpdateLandlordDateOfBirthController::class)
class UpdateLandlordDateOfBirthControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateDateOfBirthJourneyFactory

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    val updateDateOfBirthRoute =
        UpdateLandlordDateOfBirthController.UPDATE_DATE_OF_BIRTH_ROUTE +
            "/${DateOfBirthStep.ROUTE_SEGMENT}"

    @Test
    fun `getUpdateStep returns a redirect for unauthenticated user`() {
        mvc.get(updateDateOfBirthRoute).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getUpdateStep returns 403 for an unauthorised user`() {
        mvc
            .get(updateDateOfBirthRoute)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getUpdateStep returns 200 for a landlord user`() {
        whenever(
            mockJourneyFactory.createJourneySteps(),
        ).thenReturn(mapOf(DateOfBirthStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))
        mvc
            .get(updateDateOfBirthRoute)
            .andExpect {
                status { isOk() }
            }
    }
}
