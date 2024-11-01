package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
        @RequestParam(value = "page", required = false) page: Int = 1,
    ): String {
        val currentUserLocalAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!

        val pagedUserList =
            localAuthorityDataService.getPaginatedUsersAndInvitations(
                currentUserLocalAuthority,
                page - 1,
            )

        model.addAttribute("localAuthority", currentUserLocalAuthority.name)
        model.addAttribute("serviceName", SERVICE_NAME)
        model.addAttribute("userList", pagedUserList)
        model.addAttribute("totalPages", pagedUserList.totalPages)
        model.addAttribute("currentPage", page)

        return "manageLAUsers"
    }
}
