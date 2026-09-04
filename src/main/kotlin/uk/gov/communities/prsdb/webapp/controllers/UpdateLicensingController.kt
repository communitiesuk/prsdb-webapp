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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.UPDATE_LICENSING_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.UpdateLicensingController.Companion.UPDATE_LICENSING_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_LICENSING_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class UpdateLicensingController(
    private val journeyFactory: UpdateLicensingJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
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
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = {
                journeyFactory.createJourneySteps(
                    propertyOwnershipId,
                    PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId),
                    sendsUpdateEmails = true,
                )
            },
            initialiseJourney = { journeyFactory.initializeJourneyState(Pair(propertyOwnershipId, principal)) },
            dispatch = dispatch,
        )

    companion object {
        const val UPDATE_LICENSING_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{propertyOwnershipId}/$UPDATE_LICENSING_PATH_SEGMENT"

        fun getUpdateLicensingBaseRoute(propertyOwnershipId: Long): String =
            UriTemplate(UPDATE_LICENSING_ROUTE).expand(propertyOwnershipId).toASCIIString()
    }
}
