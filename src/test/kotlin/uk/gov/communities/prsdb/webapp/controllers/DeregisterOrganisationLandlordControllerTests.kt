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
import uk.gov.communities.prsdb.webapp.controllers.DeregisterOrganisationLandlordController.Companion.ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordDeregistration.OrganisationLandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordDeregistration.stepConfig.AreYouSureStep

@WebMvcTest(DeregisterOrganisationLandlordController::class)
class DeregisterOrganisationLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var organisationLandlordDeregistrationJourneyFactory: OrganisationLandlordDeregistrationJourneyFactory

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc
            .get("$ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user who is not a landlord`() {
        mvc
            .get("$ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 200 for a landlord user`() {
        whenever(
            organisationLandlordDeregistrationJourneyFactory.createJourneySteps(),
        ).thenReturn(mapOf(AreYouSureStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .get("$ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for an unknown step name`() {
        whenever(
            organisationLandlordDeregistrationJourneyFactory.createJourneySteps(),
        ).thenReturn(mapOf(AreYouSureStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

        mvc
            .get("$ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE/unknown-step")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"

        whenever(organisationLandlordDeregistrationJourneyFactory.createJourneySteps())
            .thenThrow(NoSuchJourneyException())
        whenever(organisationLandlordDeregistrationJourneyFactory.initializeJourneyState()).thenReturn(journeyId)

        mvc
            .get("$ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl("$ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}?journeyId=$journeyId")
            }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 200 for an authenticated user without the landlord role`() {
        mvc
            .get("$ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `getConfirmation returns a redirect for an unauthenticated user`() {
        mvc
            .get("$ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { is3xxRedirection() }
            }
    }
}
