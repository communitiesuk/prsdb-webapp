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
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.LANDLORD_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService

@WebMvcTest(DeregisterLandlordController::class)
class DeregisterLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var landlordDeregistrationJourneyFactory: LandlordDeregistrationJourneyFactory

    @MockitoBean
    private lateinit var landlordDeregistrationService: LandlordDeregistrationService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc
            .get("$LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user who is not a landlord`() {
        mvc
            .get("$LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 200 for a landlord user`() {
        whenever(
            landlordDeregistrationJourneyFactory.createJourneySteps("user"),
        ).thenReturn(mapOf(AreYouSureStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .get("$LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for an unknown step name`() {
        whenever(
            landlordDeregistrationJourneyFactory.createJourneySteps("user"),
        ).thenReturn(mapOf(AreYouSureStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

        mvc
            .get("$LANDLORD_DEREGISTRATION_ROUTE/unknown-step")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"

        whenever(landlordDeregistrationJourneyFactory.createJourneySteps("user"))
            .thenThrow(NoSuchJourneyException())
        whenever(landlordDeregistrationJourneyFactory.initializeJourneyState()).thenReturn(journeyId)

        mvc
            .get("$LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl("$LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}?journeyId=$journeyId")
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 200 if the landlord was deregistered in the session`() {
        whenever(landlordDeregistrationService.hasLandlordDeregisteredInThisSession()).thenReturn(true)
        whenever(landlordDeregistrationService.getLandlordHadActivePropertiesFromSession()).thenReturn(false)

        mvc
            .get("$LANDLORD_DEREGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 404 if the landlord has not deregistered in the session`() {
        whenever(landlordDeregistrationService.hasLandlordDeregisteredInThisSession()).thenReturn(false)

        mvc
            .get("$LANDLORD_DEREGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isNotFound() }
            }
    }
}
