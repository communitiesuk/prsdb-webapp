package uk.gov.communities.prsdb.webapp.controllers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import java.security.Principal

@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/manage-users")
class ManageLocalAuthorityUsersController(
    val localAuthorityDataService: LocalAuthorityDataService,
) {
    @GetMapping
    fun index(
        model: Model,
        principal: Principal,
    ): String {
        val currentUserLocalAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)
        if (currentUserLocalAuthority?.id != null) {
            // We should always get to here as only LA_ADMINs can access this page
            val users = localAuthorityDataService.getLocalAuthorityUsersForLocalAuthority(currentUserLocalAuthority.id)
            val usersJson = Json.encodeToString(users)
            model.addAttribute("usersJson", usersJson)
        }

        model.addAttribute("contentHeader", "Manage Local Authority Users")
        model.addAttribute("title", "Manage Local Authority Users")
        model.addAttribute("serviceName", SERVICE_NAME)

        return "manageLAUsers"
    }
}
