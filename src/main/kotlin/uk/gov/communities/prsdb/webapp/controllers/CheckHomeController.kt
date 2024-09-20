package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME

@Controller
@RequestMapping("/check")
class CheckHomeController {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("contentHeader", "Check a home to rent")
        model.addAttribute("title", "Check a home to rent")
        model.addAttribute("serviceName", SERVICE_NAME)
        return "index"
    }
}
