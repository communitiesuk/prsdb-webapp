package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_TABLE_PAGE
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import java.security.Principal
import java.util.Locale
import kotlin.math.ceil

@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/manage-users")
class ManageLocalAuthorityUsersController(
    val localAuthorityDataService: LocalAuthorityDataService,
    @Qualifier("messageSource") private val messageSource: MessageSource,
) {
    @GetMapping
    fun index(
        model: Model,
        principal: Principal,
        @RequestParam(value = "page", required = false) page: Int = 1,
        httpServletRequest: HttpServletRequest,
    ): String {
        val currentUserLocalAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!

        val nActiveUsers = localAuthorityDataService.countActiveLocalAuthorityUsersForLocalAuthority(currentUserLocalAuthority)
        val nPendingUsers = localAuthorityDataService.countPendingLocalAuthorityUsersForLocalAuthority(currentUserLocalAuthority)
        val totalUsers = nActiveUsers + nPendingUsers
        val totalPages = ceil((totalUsers.toDouble() / MAX_ENTRIES_IN_TABLE_PAGE.toDouble())).toInt()

        val shouldPaginate = totalPages > 1

        val pagedUserList =
            localAuthorityDataService.getUserList(
                currentUserLocalAuthority,
                page,
                nActiveUsers,
                shouldPaginate,
            )

        model.addAttribute("localAuthority", currentUserLocalAuthority.name)
        model.addAttribute("title", messageSource.getMessage("manageLAUsers.title", null, Locale("en")))
        model.addAttribute("serviceName", SERVICE_NAME)
        model.addAttribute("userList", pagedUserList)
        model.addAttribute(
            "tableColumnHeadings",
            listOf(
                messageSource.getMessage("manageLAUsers.table.column1Heading", null, Locale("en")),
                messageSource.getMessage("manageLAUsers.table.column2Heading", null, Locale("en")),
                messageSource.getMessage("manageLAUsers.table.column3Heading", null, Locale("en")),
                "",
            ),
        )
        model.addAttribute("shouldPaginate", shouldPaginate)
        model.addAttribute("totalPages", totalPages)
        model.addAttribute("currentPage", page)
        model.addAttribute("baseUri", httpServletRequest.requestURI)

        return "manageLAUsers"
    }
}
