package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DelegateToLettingAgentController.Companion.DELEGATE_TO_LETTING_AGENT_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.DelegateToLettingAgentJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.stepConfig.AllowLettingAgentStep
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(DELEGATE_TO_LETTING_AGENT_ROUTE)
class DelegateToLettingAgentController(
    private val delegateToLettingAgentJourneyFactory: DelegateToLettingAgentJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @GetMapping("/{*stepPath}")
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    fun getJourneyStep(
        @PathVariable stepPath: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
    ): ModelAndView {
        propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(propertyOwnershipId)
        return dispatchJourneyStep(stepPath, propertyOwnershipId) { getStepModelAndView() }
    }

    @PostMapping("/{*stepPath}")
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    fun postJourneyData(
        @PathVariable stepPath: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        @RequestParam formData: FormData,
    ): ModelAndView {
        propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(propertyOwnershipId)
        return dispatchJourneyStep(stepPath, propertyOwnershipId) { postStepModelAndView(formData) }
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        propertyOwnershipId: Long,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { delegateToLettingAgentJourneyFactory.createJourneySteps(propertyOwnershipId) },
            initialiseJourney = { delegateToLettingAgentJourneyFactory.initializeJourneyState(propertyOwnershipId) },
            dispatch = dispatch,
            startNewJourneyOn = { it is PropertyOwnershipMismatchException },
        )

    // TODO: PDJB-1410 - Add the session guard and build the real confirmation page content
    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    fun getConfirmation(
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
    ): String {
        propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(propertyOwnershipId)
        return "delegateToLettingAgentConfirmation"
    }

    companion object {
        const val DELEGATE_TO_LETTING_AGENT_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$DELEGATE_TO_LETTING_AGENT_JOURNEY_URL/{propertyOwnershipId}"

        fun getDelegateToLettingAgentBasePath(propertyOwnershipId: Long): String =
            UriTemplate(DELEGATE_TO_LETTING_AGENT_ROUTE)
                .expand(propertyOwnershipId)
                .toASCIIString()

        fun getDelegateToLettingAgentPath(propertyOwnershipId: Long): String =
            "${getDelegateToLettingAgentBasePath(propertyOwnershipId)}/${AllowLettingAgentStep.ROUTE_SEGMENT}"
    }
}
