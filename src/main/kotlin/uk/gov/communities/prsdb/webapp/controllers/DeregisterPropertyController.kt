package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController.Companion.PROPERTY_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDeregistrationJourney
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@Controller
@RequestMapping(PROPERTY_DEREGISTRATION_ROUTE)
class DeregisterPropertyController(
    private val propertyDeregistrationJourney: PropertyDeregistrationJourney,
) {
    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
    ): ModelAndView =
        propertyDeregistrationJourney.getModelAndViewForStep(
            stepName,
            subpage,
        )

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
    }
}
