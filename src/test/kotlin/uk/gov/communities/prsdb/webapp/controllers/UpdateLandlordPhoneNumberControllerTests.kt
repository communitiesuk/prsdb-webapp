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
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.phoneNumber.UpdatePhoneNumberJourneyFactory

@WebMvcTest(UpdateLandlordPhoneNumberController::class)
class UpdateLandlordPhoneNumberControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdatePhoneNumberJourneyFactory

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    val updatePhoneNumberRoute =
        UpdateLandlordPhoneNumberController.UPDATE_PHONE_NUMBER_ROUTE +
            "/${PhoneNumberStep.ROUTE_SEGMENT}"

    @Test
    fun `getJourneyStep returns a redirect for unauthenticated user`() {
        mvc.get(updatePhoneNumberRoute).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for an unauthorised user`() {
        mvc
            .get(updatePhoneNumberRoute)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 200 for a landlord user`() {
        whenever(
            mockJourneyFactory.createJourneySteps(),
        ).thenReturn(mapOf(PhoneNumberStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))
        mvc
            .get(updatePhoneNumberRoute)
            .andExpect {
                status { isOk() }
            }
    }
}
