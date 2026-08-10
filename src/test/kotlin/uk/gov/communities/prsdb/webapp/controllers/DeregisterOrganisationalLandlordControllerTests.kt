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
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterOrganisationalLandlordController.Companion.ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.OrganisationalLandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig.AreYouSureStep

@WebMvcTest(DeregisterOrganisationalLandlordController::class)
class DeregisterOrganisationalLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var organisationalLandlordDeregistrationJourneyFactory: OrganisationalLandlordDeregistrationJourneyFactory

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc
            .get("$ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user who is not a landlord`() {
        mvc
            .get("$ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 200 for a landlord user`() {
        whenever(
            organisationalLandlordDeregistrationJourneyFactory.createJourneySteps(),
        ).thenReturn(mapOf(AreYouSureStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .get("$ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for an unknown step name`() {
        whenever(
            organisationalLandlordDeregistrationJourneyFactory.createJourneySteps(),
        ).thenReturn(mapOf(AreYouSureStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

        mvc
            .get("$ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE/unknown-step")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"

        whenever(organisationalLandlordDeregistrationJourneyFactory.createJourneySteps())
            .thenThrow(NoSuchJourneyException())
        whenever(organisationalLandlordDeregistrationJourneyFactory.initializeJourneyState()).thenReturn(journeyId)

        mvc
            .get("$ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl("$ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}?journeyId=$journeyId")
            }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 200 for an authenticated user without the landlord role`() {
        mvc
            .get("$ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `getConfirmation returns a redirect for an unauthenticated user`() {
        mvc
            .get("$ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { is3xxRedirection() }
            }
    }
}
