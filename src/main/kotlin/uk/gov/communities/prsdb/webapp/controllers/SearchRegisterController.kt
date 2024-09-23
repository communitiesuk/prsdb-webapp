package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME

@Controller
@RequestMapping("/search")
@PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
class SearchRegisterController {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("contentHeader", "Search for Private Rented Sector information")
        model.addAttribute("title", "Search for Private Rented Sector information")
        model.addAttribute("serviceName", SERVICE_NAME)
        return "index"
    }
}
