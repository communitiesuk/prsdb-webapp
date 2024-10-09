package uk.gov.communities.prsdb.webapp.controllers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.services.ManageLocalAuthorityUserService

// @PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/manage-users")
class ManageLocalAuthorityUsersController(
    val manageLocalAuthorityUserService: ManageLocalAuthorityUserService,
) {
    @GetMapping
    fun index(model: Model): String {
        val users = manageLocalAuthorityUserService.getLocalAuthorityUsersForLocalAuthority(1)
        val usersJson = Json.encodeToString(users)
        model.addAttribute("contentHeader", "Manage Local Authority Users")
        model.addAttribute("title", "Manage Local Authority Users")
        model.addAttribute("serviceName", SERVICE_NAME)
        model.addAttribute("usersJson", usersJson)
        return "manageLAUsers"
    }
}
