package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME

// @PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/manage-users")
class ManageLAUsersController {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("contentHeader", "Manage Local Authority Users")
        model.addAttribute("title", "Manage Local Authority Users")
        model.addAttribute("serviceName", SERVICE_NAME)
        return "manageLAUsers"
    }
}
