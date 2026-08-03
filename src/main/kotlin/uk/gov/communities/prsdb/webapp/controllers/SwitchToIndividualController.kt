package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SWITCHED_TO_INDIVIDUAL_PROPERTY_ID
import uk.gov.communities.prsdb.webapp.constants.SWITCH_TO_INDIVIDUAL_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.SwitchToIndividualController.Companion.SWITCH_TO_INDIVIDUAL_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.HasPendingInvitationsStep
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.SwitchToIndividualJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(SWITCH_TO_INDIVIDUAL_ROUTE)
class SwitchToIndividualController(
    private val switchToIndividualJourneyFactory: SwitchToIndividualJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
    ): ModelAndView {
        throwExceptionIfUnauthorized(propertyOwnershipId, principal)
        return dispatchJourneyStep(stepPath, propertyOwnershipId) { getStepModelAndView() }
    }

    @PostMapping("/{*stepPath}")
    fun postJourneyData(
        @PathVariable stepPath: String,
        @PathVariable propertyOwnershipId: Long,
        @RequestParam formData: FormData,
        principal: Principal,
    ): ModelAndView {
        throwExceptionIfUnauthorized(propertyOwnershipId, principal)
        return dispatchJourneyStep(stepPath, propertyOwnershipId) { postStepModelAndView(formData) }
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        propertyOwnershipId: Long,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { switchToIndividualJourneyFactory.createJourneySteps(propertyOwnershipId) },
            initialiseJourney = { switchToIndividualJourneyFactory.initializeJourneyState(propertyOwnershipId) },
            dispatch = dispatch,
            startNewJourneyOn = { it is PropertyOwnershipMismatchException },
        )

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getSuccess(
        model: Model,
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
        session: HttpSession,
    ): String {
        throwExceptionIfUnauthorized(propertyOwnershipId, principal)

        val switchedId = session.getAttribute(SWITCHED_TO_INDIVIDUAL_PROPERTY_ID) as? Long
        if (switchedId != propertyOwnershipId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        val address = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId).address.singleLineAddress
        model.addAttribute("address", address)
        model.addAttribute("propertyDetailsUrl", PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId))

        return "switchToIndividualSuccess"
    }

    private fun throwExceptionIfUnauthorized(
        propertyOwnershipId: Long,
        principal: Principal,
    ) {
        if (!propertyOwnershipService.getIsLandlord(propertyOwnershipId, principal.name)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    companion object {
        const val SWITCH_TO_INDIVIDUAL_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{propertyOwnershipId}/$SWITCH_TO_INDIVIDUAL_JOURNEY_URL"

        fun getSwitchToIndividualBasePath(propertyOwnershipId: Long): String =
            UriTemplate(SWITCH_TO_INDIVIDUAL_ROUTE)
                .expand(propertyOwnershipId)
                .toASCIIString()

        fun getSwitchToIndividualFirstStepPath(propertyOwnershipId: Long): String =
            "${getSwitchToIndividualBasePath(propertyOwnershipId)}/${HasPendingInvitationsStep.ROUTE_SEGMENT}"
    }
}
