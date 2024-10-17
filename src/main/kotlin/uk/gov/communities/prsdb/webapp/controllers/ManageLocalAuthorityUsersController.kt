package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
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
        val currentUserLocalAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!

        val activeUsers = localAuthorityDataService.getLocalAuthorityUsersForLocalAuthority(currentUserLocalAuthority)
        // TODO: Get these from LocalAuthorityUserInvitation with userName=email, isManager=false and isPending=true
        val pendingUsers =
            listOf(
                LocalAuthorityUserDataModel("Invited user 1", isManager = false, isPending = true),
                LocalAuthorityUserDataModel("Invited user 2", isManager = false, isPending = true),
            )

        val users = activeUsers + pendingUsers

        model.addAttribute("contentHeader", "Manage ${currentUserLocalAuthority?.name}'s users")
        model.addAttribute("title", "Manage Local Authority Users")
        model.addAttribute("serviceName", SERVICE_NAME)
        model.addAttribute("userList", users)
        model.addAttribute("tableColumnHeadings", listOf("Username", "Access level", "Account status", ""))

        return "manageLAUsers"
    }
}
