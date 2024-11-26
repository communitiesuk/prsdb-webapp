package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyRegistrationJourney
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@Controller
@RequestMapping("/$REGISTER_PROPERTY_JOURNEY_URL")
class RegisterPropertyController(
    var propertyRegistrationJourney: PropertyRegistrationJourney,
) {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute(
            "registerPropertyInitialStep",
            "/$REGISTER_PROPERTY_JOURNEY_URL/${propertyRegistrationJourney.initialStepId.urlPathSegment}",
        )
        model.addAttribute("backUrl", "/")

        return "registerPropertyStartPage"
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
    ): String =
        propertyRegistrationJourney.populateModelAndGetViewName(
            propertyRegistrationJourney.getStepId(stepName),
            model,
            subpage,
        )

    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): String =
        propertyRegistrationJourney.updateJourneyDataAndGetViewNameOrRedirect(
            propertyRegistrationJourney.getStepId(stepName),
            formData,
            model,
            subpage,
            principal,
        )
}
