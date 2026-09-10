package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.INVALID_LINK_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.LettingAgentInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.StartStep
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import java.util.UUID

@PrsdbController
@RequestMapping(LETTING_AGENT_INVITATION_ROUTE)
class LettingAgentInvitationController(
    private val journeyFactory: LettingAgentInvitationJourneyFactory,
    private val lettingAgentAccessService: LettingAgentAccessService,
) {
    @GetMapping
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    fun startJourney(
        @RequestParam(value = TOKEN, required = false) rawToken: String?,
    ): ModelAndView {
        val token =
            try {
                rawToken?.let(UUID::fromString)
            } catch (_: IllegalArgumentException) {
                null
            }

        if (token == null || lettingAgentAccessService.getInvitationByTokenOrNull(token) == null) {
            return redirectToInvalidLink()
        }

        val journeyId = journeyFactory.initializeJourneyState(token)
        lettingAgentAccessService.addJourneyIdInvitationTokenPairToSession(journeyId, token.toString())
        val startUrl =
            JourneyStateService.urlWithJourneyState(
                "$LETTING_AGENT_INVITATION_ROUTE/${StartStep.ROUTE_SEGMENT}",
                journeyId,
            )
        return ModelAndView("redirect:$startUrl")
    }

    @GetMapping("/{*stepPath}")
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    fun getJourneyStep(
        @PathVariable stepPath: String,
    ): ModelAndView = dispatchJourneyStep(stepPath) { getStepModelAndView() }

    @PostMapping("/{*stepPath}")
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    fun postJourneyData(
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView = dispatchJourneyStep(stepPath) { postStepModelAndView(formData) }

    @GetMapping("/$INVALID_LINK_PAGE_PATH_SEGMENT")
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    fun invalidLink(): ModelAndView {
        // TODO PDJB-1854: Replace the invalid-link placeholder with final content.
        return ModelAndView(
            "forms/todoNoButton",
            mapOf(
                "todoComment" to "TODO: PDJB-1854: Invalid link page",
            ),
        )
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleUninitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps() },
            dispatch = dispatch,
            getRedirect = { ModelAndView("redirect:$LETTING_AGENT_INVITATION_ROUTE") },
        )

    private fun redirectToInvalidLink(): ModelAndView =
        ModelAndView("redirect:$LETTING_AGENT_INVITATION_ROUTE/$INVALID_LINK_PAGE_PATH_SEGMENT")

    companion object {
        const val LETTING_AGENT_INVITATION_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$LETTING_AGENT_PATH_SEGMENT/$LETTING_AGENT_INVITATION_PATH_SEGMENT"
    }
}
