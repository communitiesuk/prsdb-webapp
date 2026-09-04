package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateRentIncludesBillsController.Companion.LETTING_AGENT_UPDATE_RENT_INCLUDES_BILLS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentIncludesBills.UpdateRentIncludesBillsJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.util.UUID

@PrsdbController
@RequestMapping(LETTING_AGENT_UPDATE_RENT_INCLUDES_BILLS_ROUTE)
class LettingAgentUpdateRentIncludesBillsController(
    private val journeyFactory: UpdateRentIncludesBillsJourneyFactory,
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    @GetMapping("/{*stepPath}")
    fun getUpdateStep(
        @PathVariable token: UUID,
        @PathVariable stepPath: String,
    ): ModelAndView = dispatchJourneyStep(stepPath, token) { getStepModelAndView() }

    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    @PostMapping("/{*stepPath}")
    fun postUpdateStep(
        @PathVariable token: UUID,
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView = dispatchJourneyStep(stepPath, token) { postStepModelAndView(formData) }

    private fun dispatchJourneyStep(
        stepPath: String,
        token: UUID,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView {
        val propertyOwnershipId =
            lettingAgentAccessService.getInvitationByTokenOrNull(token)?.propertyOwnership?.id
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No letting agent access found for token $token")

        propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(propertyOwnershipId)

        val returnUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)

        return JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps(propertyOwnershipId, returnUrl) },
            initialiseJourney = { journeyFactory.initializeJourneyState(token) },
            dispatch = dispatch,
        )
    }

    companion object {
        const val LETTING_AGENT_UPDATE_RENT_INCLUDES_BILLS_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$LETTING_AGENT_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{token}/update-rent-includes-bills"

        fun getUpdateRentIncludesBillsRoute(token: UUID): String =
            UriTemplate(LETTING_AGENT_UPDATE_RENT_INCLUDES_BILLS_ROUTE).expand(token).toASCIIString()
    }
}
