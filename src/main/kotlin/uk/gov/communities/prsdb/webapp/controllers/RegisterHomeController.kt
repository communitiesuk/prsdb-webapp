package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.WebController
import uk.gov.communities.prsdb.webapp.models.viewModels.NavigationLinkViewModel

@WebController
@RequestMapping("/registration")
@PreAuthorize("hasRole('LANDLORD')")
class RegisterHomeController {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("title", "registerAHome")
        model.addAttribute("contentHeader", "registerAHome")
        model.addAttribute(
            "navLinks",
            listOf(
                NavigationLinkViewModel("#", "registerAHome.navLink.one", false),
                NavigationLinkViewModel("#", "registerAHome.navLink.two", true),
            ),
        )
        return "index"
    }
}
