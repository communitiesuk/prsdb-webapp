package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.AcceptInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.ValidateTokenStep
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

@PrsdbController
@RequestMapping(ACCEPT_INVITATION_ROUTE)
class AcceptInvitationController(
    private val journeyFactory: AcceptInvitationJourneyFactory,
    private val invitationService: OrganisationalLandlordInvitationService,
) {
    @GetMapping
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    fun startJourney(
        @RequestParam(value = TOKEN, required = false) token: String?,
    ): ModelAndView {
        val journeyId = journeyFactory.initializeJourneyState()
        // TODO PDJB-1822: Make the token param required and validate it here, before the journey starts
        //  (redirect to the invalid link page if it is missing or doesn't match an invitation),
        //  rather than accepting a null token and only discovering it is unresolvable in the journey.
        token?.let { invitationService.addJourneyIdInvitationTokenPairToSession(journeyId, it) }
        val startUrl =
            JourneyStateService.urlWithJourneyState(
                "$ACCEPT_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}",
                journeyId,
            )
        return ModelAndView("redirect:$startUrl")
    }

    @GetMapping("/{*stepPath}")
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    fun getJourneyStep(
        @PathVariable stepPath: String,
    ): ModelAndView = dispatchJourneyStep(stepPath) { getStepModelAndView() }

    @PostMapping("/{*stepPath}")
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    fun postJourneyData(
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView = dispatchJourneyStep(stepPath) { postStepModelAndView(formData) }

    private fun dispatchJourneyStep(
        stepPath: String,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleUninitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps() },
            dispatch = dispatch,
            getRedirect = { ModelAndView("redirect:$ACCEPT_INVITATION_ROUTE") },
        )

    companion object {
        const val ACCEPT_INVITATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/accept-invitation"
    }
}
