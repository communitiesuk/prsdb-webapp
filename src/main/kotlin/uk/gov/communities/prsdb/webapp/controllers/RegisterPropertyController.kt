package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL

@PreAuthorize("hasRole('LANDLORD')")
@Controller
@RequestMapping("/$REGISTER_PROPERTY_JOURNEY_URL")
class RegisterPropertyController {
    @GetMapping
    fun index(model: Model): String {
        /*model.addAttribute(
            "registerPropertyInitialStep",
            "/$REGISTER_PROPERTY_JOURNEY_URL/${propetyRegistrationJourney.initialStepId.urlPathSegment}",
        )*/
        model.addAttribute("registerPropertyInitialStep", "#")
        return "registerPropertyStartPage"
    }
}
