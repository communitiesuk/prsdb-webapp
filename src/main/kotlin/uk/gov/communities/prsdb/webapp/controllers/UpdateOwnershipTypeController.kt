package uk.gov.communities.prsdb.webapp.controllers

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
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipTypeController.Companion.UPDATE_OWNERSHIP_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType.UpdateOwnershipTypeJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_OWNERSHIP_TYPE_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class UpdateOwnershipTypeController(
    private val journeyFactory: UpdateOwnershipTypeJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @GetMapping("/{*stepPath}")
    fun getUpdateStep(
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable stepPath: String,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        return dispatchJourneyStep(stepPath, propertyOwnershipId, principal) { getStepModelAndView() }
    }

    @PostMapping("/{*stepPath}")
    fun postUpdateStep(
        model: Model,
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        return dispatchJourneyStep(stepPath, propertyOwnershipId, principal) { postStepModelAndView(formData) }
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        propertyOwnershipId: Long,
        principal: Principal,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps(propertyOwnershipId) },
            initialiseJourney = { journeyFactory.initializeJourneyState(propertyOwnershipId, principal) },
            dispatch = dispatch,
        )

    private fun throwErrorIfUserIsNotAuthorized(
        baseUserId: String,
        propertyOwnershipId: Long,
    ) {
        if (!propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, baseUserId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User $baseUserId is not authorized to update property ownership $propertyOwnershipId",
            )
        }
    }

    companion object {
        const val UPDATE_OWNERSHIP_TYPE_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{propertyOwnershipId}/update-ownership-type"

        fun getUpdateOwnershipTypeRoute(propertyOwnershipId: Long): String =
            UriTemplate(UPDATE_OWNERSHIP_TYPE_ROUTE).expand(propertyOwnershipId).toASCIIString()
    }
}
