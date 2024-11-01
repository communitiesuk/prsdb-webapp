package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import java.security.Principal

@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/local-authority/{localAuthorityId}/manage-users")
class ManageLocalAuthorityUsersController(
    val localAuthorityDataService: LocalAuthorityDataService,
) {
    @GetMapping
    fun index(
        @PathVariable localAuthorityId: Int,
        model: Model,
        principal: Principal,
        @RequestParam(value = "page", required = false) page: Int = 1,
    ): String {
        val currentUserLocalAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!
        if (currentUserLocalAuthority.id != localAuthorityId) {
            throw AccessDeniedException(
                "Local authority user for LA ${currentUserLocalAuthority.id} tried to manage users for LA $localAuthorityId",
            )
        }

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
