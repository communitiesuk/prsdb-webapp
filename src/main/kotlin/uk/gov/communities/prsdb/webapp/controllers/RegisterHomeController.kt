package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.models.dataModels.NavigationLinkDataModel

@Controller
@RequestMapping("/registration")
@PreAuthorize("hasRole('LANDLORD')")
class RegisterHomeController {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("contentHeader", "Register a home to rent")
        model.addAttribute("title", "Register a home to rent")
        model.addAttribute(
            "navLinks",
            listOf(
                NavigationLinkDataModel("#", "registerAHome.navLink.one", false),
                NavigationLinkDataModel("#", "registerAHome.navLink.two", true),
            ),
        )
        return "index"
    }
}
