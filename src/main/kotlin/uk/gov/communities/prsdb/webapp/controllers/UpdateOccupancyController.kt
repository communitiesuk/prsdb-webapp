package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController.Companion.UPDATE_OCCUPANCY_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_OCCUPANCY_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class UpdateOccupancyController(
    private val journeyFactory: UpdateOccupancyJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val featureFlagManager: FeatureFlagManager,
) {
    @GetMapping("/{*stepPath}")
    fun getUpdateStep(
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable stepPath: String,
    ): ModelAndView {
        propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(propertyOwnershipId)
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
        propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(propertyOwnershipId)
        return dispatchJourneyStep(stepPath, propertyOwnershipId, principal) { postStepModelAndView(formData) }
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        propertyOwnershipId: Long,
        principal: Principal,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView {
        val withCya = featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)
        return JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps(propertyOwnershipId, withCya) },
            initialiseJourney = { journeyFactory.initializeJourneyState(propertyOwnershipId, principal) },
            dispatch = dispatch,
        )
    }

    companion object {
        const val UPDATE_OCCUPANCY_ROUTE = "/$LANDLORD_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{propertyOwnershipId}/update-occupancy"

        fun getUpdateOccupancyRoute(propertyOwnershipId: Long): String =
            UPDATE_OCCUPANCY_ROUTE.replace("{propertyOwnershipId}", propertyOwnershipId.toString())
    }
}
