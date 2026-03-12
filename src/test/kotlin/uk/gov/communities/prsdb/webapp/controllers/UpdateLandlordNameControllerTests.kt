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
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.name.UpdateNameJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep

@WebMvcTest(UpdateLandlordNameController::class)
class UpdateLandlordNameControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateNameJourneyFactory

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    val updateNameRoute =
        UpdateLandlordNameController.UPDATE_NAME_ROUTE +
            "/${NameStep.ROUTE_SEGMENT}"

    @Test
    fun `getUpdateStep returns a redirect for unauthenticated user`() {
        mvc.get(updateNameRoute).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getUpdateStep returns 403 for an unauthorised user`() {
        mvc
            .get(updateNameRoute)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getUpdateStep returns 200 for a landlord user`() {
        whenever(
            mockJourneyFactory.createJourneySteps(),
        ).thenReturn(mapOf(NameStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))
        mvc
            .get(updateNameRoute)
            .andExpect {
                status { isOk() }
            }
    }
}
