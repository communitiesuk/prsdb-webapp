package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import java.security.Principal

@Controller
@RequestMapping("/${REGISTER_LANDLORD_JOURNEY_URL}")
class RegisterLandlordController(
    var landlordRegistrationJourney: LandlordRegistrationJourney,
) {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute(
            "registerAsALandlordInitialStep",
            "/${REGISTER_LANDLORD_JOURNEY_URL}/${landlordRegistrationJourney.initialStepId.urlPathSegment}",
        )
        return "registerAsALandlord"
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
    ): String =
        landlordRegistrationJourney.populateModelAndGetViewName(
            landlordRegistrationJourney.getStepId(stepName),
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
        landlordRegistrationJourney.updateJourneyDataAndGetViewNameOrRedirect(
            landlordRegistrationJourney.getStepId(stepName),
            formData,
            model,
            subpage,
            principal,
        )
}
