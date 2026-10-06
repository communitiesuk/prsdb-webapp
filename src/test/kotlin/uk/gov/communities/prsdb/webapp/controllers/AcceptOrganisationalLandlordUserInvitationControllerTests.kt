package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
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
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptOrganisationalLandlordUserInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.InvalidLinkStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.JoinOrganisationStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.ValidateTokenStep
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

@WebMvcTest(AcceptOrganisationalLandlordUserInvitationController::class)
class AcceptOrganisationalLandlordUserInvitationControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: AcceptOrganisationalLandlordUserInvitationJourneyFactory

    @MockitoBean
    private lateinit var invitationService: OrganisationalLandlordInvitationService

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
        fun `startJourney initializes state and redirects for an unauthenticated user`() {
            val token = "1234abcd-5678-abcd-1234-567abcd2222a"
            whenever(journeyFactory.initializeJourneyState(token)).thenReturn(journeyId)

            val expectedRedirectUrl =
                JourneyStateService
                    .urlWithJourneyState(
                        "$ACCEPT_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}",
                        journeyId,
                    )

            mvc
                .get(ACCEPT_INVITATION_ROUTE) {
                    param("token", token)
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(expectedRedirectUrl)
                }
        }

        @Test
        @WithMockUser(value = "user")
        fun `startJourney rejects requests without a token`() {
            mvc
                .get(ACCEPT_INVITATION_ROUTE)
                .andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        @WithMockUser(value = "user")
        fun `startJourney initializes state and redirects for authenticated user without landlord role`() {
            val token = "1234abcd-5678-abcd-1234-567abcd2222a"
            whenever(journeyFactory.initializeJourneyState(token)).thenReturn(journeyId)

            val expectedRedirectUrl =
                JourneyStateService
                    .urlWithJourneyState(
                        "$ACCEPT_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}",
                        journeyId,
                    )

            mvc
                .get(ACCEPT_INVITATION_ROUTE) {
                    param("token", token)
                }
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(expectedRedirectUrl)
                }

            verify(journeyFactory).initializeJourneyState(token)
            verify(invitationService).addJourneyIdInvitationTokenPairToSession(journeyId, token)
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

        @Test
        fun `getJourneyStep returns the validate token step for an unauthenticated user`() {
            whenever(journeyFactory.createJourneySteps())
                .thenReturn(mapOf(ValidateTokenStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
            whenever(mockStepLifecycleOrchestrator.getStepModelAndView()).thenReturn(placeholderModelAndView)

            mvc
                .get("$ACCEPT_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}?$JOURNEY_ID=$journeyId")
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun `getJourneyStep returns the invalid link step for an unauthenticated user`() {
            whenever(journeyFactory.createJourneySteps())
                .thenReturn(mapOf(InvalidLinkStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
            whenever(mockStepLifecycleOrchestrator.getStepModelAndView()).thenReturn(placeholderModelAndView)

            mvc
                .get("$ACCEPT_INVITATION_ROUTE/${InvalidLinkStep.ROUTE_SEGMENT}?$JOURNEY_ID=$journeyId")
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun `getJourneyStep redirects an unauthenticated user to login for a step that requires authentication`() {
            mvc
                .get("$ACCEPT_INVITATION_ROUTE/${JoinOrganisationStep.ROUTE_SEGMENT}?$JOURNEY_ID=$journeyId")
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrlPattern("**/oauth2/authorization/one-login")
                }
        }

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
