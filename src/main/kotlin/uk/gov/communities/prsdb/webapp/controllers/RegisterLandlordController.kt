package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME

@Controller
@RequestMapping("/register-as-a-landlord")
class RegisterLandlordController {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("title", "Register as a Landlord")
        model.addAttribute("serviceName", SERVICE_NAME)
        return "registerAsALandlord"
    }
}
