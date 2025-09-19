package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRM_SIGN_OUT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SIGN_OUT_PATH_SEGMENT

@PrsdbController
class SignOutController {
    @GetMapping("/$CONFIRM_SIGN_OUT_PATH_SEGMENT")
    fun confirmSignOut(model: Model): String = "exampleConfirmSignOut"

    @GetMapping("/$SIGN_OUT_PATH_SEGMENT")
    fun signOut(model: Model): String {
        model.addAttribute("title", "signOut.title")
        model.addAttribute("contentHeader", "signOut.header")
        model.addAttribute("buttonUrl", "/")
        return "signoutPage"
    }
}
