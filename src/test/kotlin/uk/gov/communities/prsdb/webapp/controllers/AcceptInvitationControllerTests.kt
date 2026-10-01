package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.JOURNEY_ID
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.AcceptInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.JoinOrganisationStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.ValidateTokenStep

@WebMvcTest(AcceptInvitationController::class)
class AcceptInvitationControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: AcceptInvitationJourneyFactory

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    private val journeyId = "test-accept-invitation-journey-id"
    private val placeholderModelAndView = ModelAndView("placeholder", mapOf("title" to "placeholder"))

    @BeforeEach
    fun enableFeatureFlag() {
        whenever(featureFlagManager.checkFeature(MULTI_USER_ORGANISATIONS)).thenReturn(true)
    }

    @Nested
    inner class StartJourney {
        @Test
        fun `startJourney redirects unauthenticated users to login`() {
            mvc
                .get(ACCEPT_INVITATION_ROUTE)
                .andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        @WithMockUser(value = "user")
        fun `startJourney initializes journey state and redirects to join organisation step for authenticated user without landlord role`() {
            whenever(journeyFactory.initializeJourneyState()).thenReturn(journeyId)

            val expectedRedirectUrl =
                JourneyStateService
                    .urlWithJourneyState(
                        "$ACCEPT_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}",
                        journeyId,
                    )

            mvc
                .get(ACCEPT_INVITATION_ROUTE)
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(expectedRedirectUrl)
                }
        }
    }

    @Nested
    inner class GetJourneyStep {
        @Test
        @WithMockUser(value = "user")
        fun `getJourneyStep returns step view for authenticated user without landlord role`() {
            whenever(journeyFactory.createJourneySteps())
                .thenReturn(mapOf(JoinOrganisationStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
            whenever(mockStepLifecycleOrchestrator.getStepModelAndView()).thenReturn(placeholderModelAndView)

            mvc
                .get("$ACCEPT_INVITATION_ROUTE/${JoinOrganisationStep.ROUTE_SEGMENT}?$JOURNEY_ID=$journeyId")
                .andExpect {
                    status { isOk() }
                }
        }

        // TODO: add controller coverage for unauthenticated access to the validate-token step once that permission is allowed.
        // TODO: add controller coverage for unauthenticated access to the invalid-link step once that permission is allowed.
        @Test
        @WithMockUser(value = "user")
        fun `getJourneyStep returns 404 when step is not found`() {
            whenever(journeyFactory.createJourneySteps()).thenReturn(emptyMap())

            mvc
                .get("$ACCEPT_INVITATION_ROUTE/${JoinOrganisationStep.ROUTE_SEGMENT}?$JOURNEY_ID=$journeyId")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(value = "user")
        fun `getJourneyStep redirects to start route when NoSuchJourneyException is thrown`() {
            whenever(journeyFactory.createJourneySteps()).thenThrow(NoSuchJourneyException())

            mvc
                .get("$ACCEPT_INVITATION_ROUTE/${JoinOrganisationStep.ROUTE_SEGMENT}?$JOURNEY_ID=$journeyId")
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(ACCEPT_INVITATION_ROUTE)
                }
        }
    }

    @Nested
    inner class PostJourneyData {
        @Test
        @WithMockUser(value = "user")
        fun `postJourneyData submits data and redirects for authenticated user without landlord role`() {
            whenever(journeyFactory.createJourneySteps())
                .thenReturn(mapOf(JoinOrganisationStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
            whenever(mockStepLifecycleOrchestrator.postStepModelAndView(org.mockito.kotlin.any())).thenReturn(placeholderModelAndView)

            mvc
                .post("$ACCEPT_INVITATION_ROUTE/${JoinOrganisationStep.ROUTE_SEGMENT}?$JOURNEY_ID=$journeyId") {
                    param("formData", "")
                    with(csrf())
                }.andExpect {
                    status { isOk() }
                }
        }
    }
}
