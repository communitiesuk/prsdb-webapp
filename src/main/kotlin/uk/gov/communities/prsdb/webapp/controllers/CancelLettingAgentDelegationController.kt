package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
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
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REMOVE_LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CancelLettingAgentDelegationController.Companion.REMOVE_LETTING_AGENT_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.CancelLettingAgentDelegationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(REMOVE_LETTING_AGENT_ROUTE)
class CancelLettingAgentDelegationController(
    private val cancelLettingAgentDelegationJourneyFactory: CancelLettingAgentDelegationJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
        @PathVariable propertyOwnershipId: Long,
    ): ModelAndView {
        propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(propertyOwnershipId)
        return dispatchJourneyStep(stepPath, propertyOwnershipId) { getStepModelAndView() }
    }

    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    @PostMapping("/{*stepPath}")
    fun postJourneyData(
        @PathVariable stepPath: String,
        @PathVariable propertyOwnershipId: Long,
        @RequestParam formData: FormData,
    ): ModelAndView {
        propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(propertyOwnershipId)
        return dispatchJourneyStep(stepPath, propertyOwnershipId) { postStepModelAndView(formData) }
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        propertyOwnershipId: Long,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { cancelLettingAgentDelegationJourneyFactory.createJourneySteps(propertyOwnershipId) },
            initialiseJourney = { cancelLettingAgentDelegationJourneyFactory.initializeJourneyState(propertyOwnershipId) },
            dispatch = dispatch,
            startNewJourneyOn = { it is PropertyOwnershipMismatchException },
        )

    // TODO PDJB-1413: add the session guard so the confirmation page cannot be reached out of context,
    //  and build the real confirmation page content, including the onward link back to the property record.
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(
        @PathVariable propertyOwnershipId: Long,
        model: Model,
    ): String {
        propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(propertyOwnershipId)
        model.addAttribute("todoComment", "TODO PDJB-1413: letting agent or property manager removal confirmation")
        return "forms/todoConfirmation"
    }

    companion object {
        const val REMOVE_LETTING_AGENT_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{propertyOwnershipId}/$REMOVE_LETTING_AGENT_PATH_SEGMENT"

        fun getRemoveLettingAgentBasePath(propertyOwnershipId: Long): String =
            UriTemplate(REMOVE_LETTING_AGENT_ROUTE).expand(propertyOwnershipId).toASCIIString()

        fun getRemoveLettingAgentPath(propertyOwnershipId: Long): String =
            "${getRemoveLettingAgentBasePath(propertyOwnershipId)}/${AreYouSureStep.ROUTE_SEGMENT}"

        fun getRemoveLettingAgentConfirmationPath(propertyOwnershipId: Long): String =
            "${getRemoveLettingAgentBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT"
    }
}
