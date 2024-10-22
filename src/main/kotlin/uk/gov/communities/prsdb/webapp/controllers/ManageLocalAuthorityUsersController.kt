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
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
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
    val maxUsersDisplayed = 3

    @GetMapping
    fun index(
        model: Model,
        principal: Principal,
        @RequestParam(required = false) page: String?,
    ): String {
        val currentUserLocalAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!

        val nActiveUsers = localAuthorityDataService.countActiveLocalAuthorityUsersForLocalAuthority(currentUserLocalAuthority)
        val nPendingUsers = localAuthorityDataService.countPendingLocalAuthorityUsersForLocalAuthority(currentUserLocalAuthority)
        val totalUsers = nActiveUsers + nPendingUsers
        val totalPages = ceil((totalUsers.toDouble() / maxUsersDisplayed.toDouble())).toInt()

        val shouldPaginate = totalPages > 1
        val currentPageNumber = getCurrentPage(page)

        val pagedUserList = getUserList(currentUserLocalAuthority, currentPageNumber, nActiveUsers, shouldPaginate)

        model.addAttribute(
            "contentHeader",
            messageSource.getMessage("manageLAUsers.contentHeader.part1", null, Locale("en")) +
                " " + currentUserLocalAuthority.name +
                messageSource.getMessage("manageLAUsers.contentHeader.part2", null, Locale("en")),
        )
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
        model.addAttribute("currentPage", currentPageNumber)
        model.addAttribute("isLastPage", currentPageNumber == totalPages)

        return "manageLAUsers"
    }

    private fun getUserList(
        localAuthority: LocalAuthority,
        currentPageNumber: Int,
        nActiveUsers: Long,
        shouldPaginate: Boolean,
    ): List<LocalAuthorityUserDataModel> {
        var activeUsers = listOf<LocalAuthorityUserDataModel>()
        var pendingUsers = listOf<LocalAuthorityUserDataModel>()
        if (shouldPaginate) {
            val firstDisplayedUserTotalIndex = (currentPageNumber - 1) * maxUsersDisplayed
            if (firstDisplayedUserTotalIndex < nActiveUsers) {
                activeUsers = getActiveUsersPaginated(localAuthority, currentPageNumber, maxUsersDisplayed)
            }

            if (activeUsers.size < maxUsersDisplayed) {
                if (activeUsers.isNotEmpty()) {
                    val nPendingUsersOnMixedPage = maxUsersDisplayed - activeUsers.size
                    pendingUsers = getPendingUsersPaginated(localAuthority, 1, nPendingUsersOnMixedPage, 0)
                } else {
                    val nPagesWithActiveUsers = ceil(nActiveUsers.toDouble() / maxUsersDisplayed.toDouble()).toInt()
                    val pendingUserPage = currentPageNumber - nPagesWithActiveUsers

                    val nActiveUsersOnMixedPage = (nActiveUsers % maxUsersDisplayed).toInt()
                    val nPendingUsersOnMixedPage = maxUsersDisplayed - nActiveUsersOnMixedPage

                    pendingUsers =
                        getPendingUsersPaginated(
                            localAuthority,
                            pendingUserPage,
                            maxUsersDisplayed,
                            nPendingUsersOnMixedPage,
                        )
                }
            }
        } else {
            activeUsers = getActiveUsersPaginated(localAuthority, 1, maxUsersDisplayed)
            pendingUsers = getPendingUsersPaginated(localAuthority, 1, maxUsersDisplayed, 0)
        }

        return activeUsers + pendingUsers
    }

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
        initialOffset: Int,
    ): List<LocalAuthorityUserDataModel> {
        val pageRequest = MyPageRequest(page - 1, nUsers, Sort.by(Sort.Direction.ASC, "invitedEmail"), initialOffset)
        return localAuthorityDataService.getLocalAuthorityPendingUsersForLocalAuthority(localAuthority, pageRequest)
    }

    private fun getCurrentPage(pageString: String?): Int {
        if (pageString == null) return 1

        try {
            return pageString.toInt()
        } catch (e: NumberFormatException) {
            return 1
        }
    }
}

class MyPageRequest(
    pageNumber: Int,
    pageSize: Int,
    sort: Sort,
    private val initialOffset: Int,
) : PageRequest(pageNumber, pageSize, sort) {
    override fun getOffset(): Long = (this.pageNumber * this.pageSize + initialOffset).toLong()
}
