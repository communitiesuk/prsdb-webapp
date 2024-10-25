package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SignOutController {
    @GetMapping("/signout")
    fun signOut(model: Model): String {
        model.addAttribute("contentHeader", "Sign Out")
        model.addAttribute("title", "You have signed out")
        return "index"
    }
}
