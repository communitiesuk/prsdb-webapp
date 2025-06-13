package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.WebController

@WebController
@RequestMapping("/check")
class CheckHomeController {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("title", "checkHome")
        model.addAttribute("contentHeader", "checkHome")
        return "index"
    }
}
