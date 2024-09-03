package uk.gov.communities.prsd.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsd.webapp.constants.SERVICE_NAME

@Controller
@RequestMapping("/search")
class SearchRegisterController {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("contentHeader", "Search for Private Rented Sector information")
        model.addAttribute("title", "Search for Private Rented Sector information")
        model.addAttribute("serviceName", SERVICE_NAME)
        return "index"
    }
}
