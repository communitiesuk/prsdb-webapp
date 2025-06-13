package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.communities.prsdb.webapp.annotations.WebController

@WebController
class SignOutController {
    @GetMapping("/confirm-sign-out")
    fun confirmSignOut(model: Model): String = "exampleConfirmSignOut"

    @GetMapping("/signout")
    fun signOut(model: Model): String {
        model.addAttribute("title", "signOut.title")
        model.addAttribute("contentHeader", "signOut.header")
        return "index"
    }
}
