package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.INVALID_LINK_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.JOURNEY_ID
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.LettingAgentInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.StartStep
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.util.UUID

@WebMvcTest(LettingAgentInvitationController::class)
class LettingAgentInvitationControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: LettingAgentInvitationJourneyFactory

    @MockitoBean
    private lateinit var lettingAgentAccessService: LettingAgentAccessService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val validToken = UUID.randomUUID()
    private val journeyId = "test-journey-id"
    private val placeholderModelAndView = ModelAndView("placeholder", mapOf("title" to "placeholder"))

    @Nested
    inner class StartJourney {
        @Test
        fun `startJourney is accessible without authentication`() {
            whenever(lettingAgentAccessService.getInvitationByTokenOrNull(validToken))
                .thenReturn(MockLettingAgentData.createLettingAgentAccess())
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            mvc
                .get("$LETTING_AGENT_INVITATION_ROUTE?$TOKEN=$validToken")
                .andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        fun `startJourney initializes journey state and redirects to start step`() {
            whenever(lettingAgentAccessService.getInvitationByTokenOrNull(validToken))
                .thenReturn(MockLettingAgentData.createLettingAgentAccess())
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            val expectedRedirectUrl =
                JourneyStateService
                    .urlWithJourneyState(
                        "$LETTING_AGENT_INVITATION_ROUTE/${StartStep.ROUTE_SEGMENT}",
                        journeyId,
                    )

            mvc
                .get("$LETTING_AGENT_INVITATION_ROUTE?$TOKEN=$validToken")
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(expectedRedirectUrl)
                }
        }

        @Test
        fun `startJourney stores token in session`() {
            whenever(lettingAgentAccessService.getInvitationByTokenOrNull(validToken))
                .thenReturn(MockLettingAgentData.createLettingAgentAccess())
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            mvc
                .get("$LETTING_AGENT_INVITATION_ROUTE?$TOKEN=$validToken")

            verify(lettingAgentAccessService).addJourneyIdInvitationTokenPairToSession(journeyId, validToken.toString())
        }

        @Test
        fun `startJourney redirects to invalid link page when token is missing`() {
            mvc
                .get(LETTING_AGENT_INVITATION_ROUTE)
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl("$LETTING_AGENT_INVITATION_ROUTE/$INVALID_LINK_PAGE_PATH_SEGMENT")
                }
        }

        @Test
        fun `startJourney redirects to invalid link page when token is not a valid UUID`() {
            mvc
                .get("$LETTING_AGENT_INVITATION_ROUTE?$TOKEN=not-a-uuid")
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl("$LETTING_AGENT_INVITATION_ROUTE/$INVALID_LINK_PAGE_PATH_SEGMENT")
                }
        }

        @Test
        fun `startJourney redirects to invalid link page when no invitation exists for token`() {
            whenever(lettingAgentAccessService.getInvitationByTokenOrNull(validToken)).thenReturn(null)

            mvc
                .get("$LETTING_AGENT_INVITATION_ROUTE?$TOKEN=$validToken")
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl("$LETTING_AGENT_INVITATION_ROUTE/$INVALID_LINK_PAGE_PATH_SEGMENT")
                }
        }
    }

    @Nested
    inner class GetJourneyStep {
        @Test
        fun `getJourneyStep is accessible without authentication`() {
            whenever(journeyFactory.createJourneySteps())
                .thenReturn(mapOf(StartStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
            whenever(mockStepLifecycleOrchestrator.getStepModelAndView()).thenReturn(placeholderModelAndView)

            mvc
                .get("$LETTING_AGENT_INVITATION_ROUTE/${StartStep.ROUTE_SEGMENT}?$JOURNEY_ID=$journeyId")
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun `getJourneyStep returns 404 when step is not found in journey map`() {
            whenever(journeyFactory.createJourneySteps()).thenReturn(emptyMap())

            mvc
                .get("$LETTING_AGENT_INVITATION_ROUTE/${StartStep.ROUTE_SEGMENT}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun `getJourneyStep redirects to start route when NoSuchJourneyException is thrown`() {
            whenever(journeyFactory.createJourneySteps()).thenThrow(NoSuchJourneyException())

            mvc
                .get("$LETTING_AGENT_INVITATION_ROUTE/${StartStep.ROUTE_SEGMENT}")
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(LETTING_AGENT_INVITATION_ROUTE)
                }
        }
    }

    @Nested
    inner class PostJourneyData {
        @Test
        fun `postJourneyData is accessible without authentication`() {
            whenever(journeyFactory.createJourneySteps()).thenReturn(emptyMap())

            mvc
                .post("$LETTING_AGENT_INVITATION_ROUTE/${StartStep.ROUTE_SEGMENT}") {
                    param("formData", "")
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun `postJourneyData returns 404 when step is not found in journey map`() {
            whenever(journeyFactory.createJourneySteps()).thenReturn(emptyMap())

            mvc
                .post("$LETTING_AGENT_INVITATION_ROUTE/${StartStep.ROUTE_SEGMENT}") {
                    param("formData", "")
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun `postJourneyData redirects to start route when NoSuchJourneyException is thrown`() {
            whenever(journeyFactory.createJourneySteps()).thenThrow(NoSuchJourneyException())

            mvc
                .post("$LETTING_AGENT_INVITATION_ROUTE/${StartStep.ROUTE_SEGMENT}") {
                    param("formData", "")
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(LETTING_AGENT_INVITATION_ROUTE)
                }
        }
    }

    @Nested
    inner class InvalidLink {
        @Test
        fun `invalidLink is accessible without authentication`() {
            mvc
                .get("$LETTING_AGENT_INVITATION_ROUTE/$INVALID_LINK_PAGE_PATH_SEGMENT")
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun `invalidLink returns the todoNoButton view`() {
            mvc
                .get("$LETTING_AGENT_INVITATION_ROUTE/$INVALID_LINK_PAGE_PATH_SEGMENT")
                .andExpect {
                    status { isOk() }
                    view { name("forms/todoNoButton") }
                }
        }
    }
}
