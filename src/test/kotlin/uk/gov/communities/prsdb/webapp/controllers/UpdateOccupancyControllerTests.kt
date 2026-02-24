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
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(UpdateOccupancyController::class)
class UpdateOccupancyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateOccupancyJourneyFactory

    @MockitoBean
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    val propertyOwnershipId = 1L

    val updateOccupancyRoute =
        UpdateOccupancyController.getUpdateOccupancyRoute(propertyOwnershipId) +
            "/${RegisterPropertyStepId.Occupancy.urlPathSegment}"

    @Test
    fun `getUpdateStep returns a redirect for unauthenticated user`() {
        mvc.get(updateOccupancyRoute).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getUpdateStep returns 403 for an unauthorised user`() {
        mvc
            .get(updateOccupancyRoute)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getUpdateStep returns 404 for a landlord user not authorised to edit the property`() {
        whenever(mockPropertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, "user"))
            .thenReturn(false)
        mvc
            .get(updateOccupancyRoute)
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getUpdateStep returns 200 for a landlord user`() {
        whenever(mockPropertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, "user"))
            .thenReturn(true)
        whenever(
            mockJourneyFactory.createJourneySteps(propertyOwnershipId),
        ).thenReturn(mapOf(RegisterPropertyStepId.Occupancy.urlPathSegment to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))
        mvc
            .get(updateOccupancyRoute)
            .andExpect {
                status { isOk() }
            }
    }
}
