package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import java.security.Principal
import java.util.Locale

@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/manage-users")
class ManageLocalAuthorityUsersController(
    val localAuthorityDataService: LocalAuthorityDataService,
    @Qualifier("messageSource") private val messageSource: MessageSource,
) {
    val maxUsersDisplayed = 2

    @GetMapping
    fun index(
        model: Model,
        principal: Principal,
    ): String {
        val currentUserLocalAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!

        val activeUsers = getActiveUsersPaginated(currentUserLocalAuthority, 1, maxUsersDisplayed)
        val pendingUsers = getPendingUsersPaginated(currentUserLocalAuthority, 1, maxUsersDisplayed)
        val users = activeUsers + pendingUsers

        model.addAttribute(
            "contentHeader",
            messageSource.getMessage("manageLAUsers.contentHeader.part1", null, Locale("en")) +
                " " + currentUserLocalAuthority.name +
                messageSource.getMessage("manageLAUsers.contentHeader.part2", null, Locale("en")),
        )
        model.addAttribute("title", messageSource.getMessage("manageLAUsers.title", null, Locale("en")))
        model.addAttribute("serviceName", SERVICE_NAME)
        model.addAttribute("userList", users)
        model.addAttribute(
            "tableColumnHeadings",
            listOf(
                messageSource.getMessage("manageLAUsers.table.column1Heading", null, Locale("en")),
                messageSource.getMessage("manageLAUsers.table.column2Heading", null, Locale("en")),
                messageSource.getMessage("manageLAUsers.table.column3Heading", null, Locale("en")),
                "",
            ),
        )

        return "manageLAUsers"
    }

    private fun shouldPaginate(localAuthority: LocalAuthority): Boolean =
        (
            localAuthorityDataService.countActiveLocalAuthorityUsersForLocalAuthority(localAuthority) +
                localAuthorityDataService.countPendingLocalAuthorityUsersForLocalAuthority(localAuthority)
                > maxUsersDisplayed
        )

    private fun getActiveUsersPaginated(
        localAuthority: LocalAuthority,
        page: Int,
        nUsers: Int,
    ): List<LocalAuthorityUserDataModel> {
        val pageRequest = PageRequest.of(page - 1, nUsers, Sort.by(Sort.Direction.ASC, "baseUser_name"))
        return localAuthorityDataService.getLocalAuthorityUsersForLocalAuthority(localAuthority, pageRequest)
    }

    private fun getPendingUsersPaginated(
        localAuthority: LocalAuthority,
        page: Int,
        nUsers: Int,
    ): List<LocalAuthorityUserDataModel> {
        val pageRequest = PageRequest.of(page - 1, nUsers, Sort.by(Sort.Direction.ASC, "invitedEmail"))
        return localAuthorityDataService.getLocalAuthorityPendingUsersForLocalAuthority(localAuthority, pageRequest)
    }
}
