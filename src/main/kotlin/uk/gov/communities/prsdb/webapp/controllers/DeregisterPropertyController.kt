package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController.Companion.PROPERTY_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDeregistrationJourney
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@Controller
@RequestMapping(PROPERTY_DEREGISTRATION_ROUTE)
class DeregisterPropertyController(
    private val propertyDeregistrationJourney: PropertyDeregistrationJourney,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
        principal: Principal,
    ): ModelAndView {
        if (stepName == propertyDeregistrationJourney.initialStepId.urlPathSegment) {
            // TODO: PRSD-696 - At the moment you can start a dereg journey on a property you are allowed to delete
            // and still get to the reason step for one you can't delete because everything is under the same key
            // in the journeyData.
            // This might get fixed after refactoring - I think the property ownership id should get included in
            // the journey data key, but check this!
            if (!propertyOwnershipService.getIsAuthorizedToDeleteRecord(propertyOwnershipId, principal.name)) {
                throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Property ownership $propertyOwnershipId not found",
                )
            }
        }

        return propertyDeregistrationJourney.getModelAndViewForStep(
            stepName,
            subpage,
        )
    }

    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): ModelAndView =
        propertyDeregistrationJourney.completeStep(
            stepName,
            formData,
            subpage,
            principal,
        )

    companion object {
        const val PROPERTY_DEREGISTRATION_ROUTE = "/$DEREGISTER_PROPERTY_JOURNEY_URL/{propertyOwnershipId}"

        fun getPropertyDeregistrationPath(propertyOwnershipId: Long): String =
            // TODO: PRSD-696 use hte inital path segment here but maybe after we have journey factories
            UriTemplate("$PROPERTY_DEREGISTRATION_ROUTE/are-you-sure")
                .expand(propertyOwnershipId)
                .toASCIIString()
    }
}
