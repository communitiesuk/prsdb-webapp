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
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.LANDLORD_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordDeregistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDeregistrationJourneyFactory
import java.security.Principal

@Controller
@RequestMapping(LANDLORD_DEREGISTRATION_ROUTE)
class DeregisterLandlordController(
    private val landlordDeregistrationJourneyFactory: LandlordDeregistrationJourneyFactory,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping
    fun index(principal: Principal): String {
        throwExceptionIfCurrentUserIsNotALandlord(principal)

        // TODO: PRSD-703
        // Check if the landlord has any registered properties.
        // If they do, redirect to maybe the property records page for now.
        // If not, they can go into the landlord deregistration journey

        return "redirect:$LANDLORD_DEREGISTRATION_ROUTE/${LandlordDeregistrationJourney.initialStepId.urlPathSegment}"
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
        principal: Principal,
    ): ModelAndView {
        throwExceptionIfCurrentUserIsNotALandlord(principal)

        return landlordDeregistrationJourneyFactory
            .create()
            .getModelAndViewForStep(
                stepName,
                subpage,
            )
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): ModelAndView {
        throwExceptionIfCurrentUserIsNotALandlord(principal)

        return landlordDeregistrationJourneyFactory
            .create()
            .completeStep(
                stepName,
                formData,
                subpage,
                principal,
            )
    }

    private fun throwExceptionIfCurrentUserIsNotALandlord(principal: Principal) {
        // TODO: PRSD-703
    }

    companion object {
        const val LANDLORD_DEREGISTRATION_ROUTE = "/$DEREGISTER_LANDLORD_JOURNEY_URL"
    }
}
