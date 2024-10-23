package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_TABLE_PAGE
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.ConfirmedEmailDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import java.security.Principal
import kotlin.math.ceil

@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/manage-users")
class ManageLocalAuthorityUsersController(
    var emailSender: EmailNotificationService<LocalAuthorityInvitationEmail>,
    var invitationService: LocalAuthorityInvitationService,
    val localAuthorityDataService: LocalAuthorityDataService,
) {
    val maxUsersDisplayed = MAX_ENTRIES_IN_TABLE_PAGE

    @GetMapping
    fun index(
        model: Model,
        principal: Principal,
        @RequestParam(required = false) page: String?,
        httpServletRequest: HttpServletRequest,
    ): String {
        val currentUserLocalAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!

        val nActiveUsers = localAuthorityDataService.countActiveLocalAuthorityUsersForLocalAuthority(currentUserLocalAuthority)
        val nPendingUsers = localAuthorityDataService.countPendingLocalAuthorityUsersForLocalAuthority(currentUserLocalAuthority)
        val totalUsers = nActiveUsers + nPendingUsers
        val totalPages = ceil((totalUsers.toDouble() / maxUsersDisplayed.toDouble())).toInt()

        val shouldPaginate = totalPages > 1
        val currentPageNumber = getCurrentPage(page)

        val pagedUserList = getUserList(currentUserLocalAuthority, currentPageNumber, nActiveUsers, shouldPaginate)

        model.addAttribute("localAuthority", currentUserLocalAuthority.name)
        model.addAttribute("userList", pagedUserList)
        model.addAttribute(
            "tableColumnHeadings",
            listOf(
                "manageLAUsers.table.column1Heading",
                "manageLAUsers.table.column2Heading",
                "manageLAUsers.table.column3Heading",
                "",
            ),
        )
        model.addAttribute("shouldPaginate", shouldPaginate)
        model.addAttribute("totalPages", totalPages)
        model.addAttribute("currentPage", currentPageNumber)
        model.addAttribute("isLastPage", currentPageNumber == totalPages)
        model.addAttribute("baseUri", httpServletRequest.requestURI)

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
            val firstDisplayedUserIndex = (currentPageNumber - 1) * maxUsersDisplayed
            if (firstDisplayedUserIndex < nActiveUsers) {
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

    @GetMapping("/invite-new-user")
    fun exampleEmailPage(model: Model): String {
        model.addAttribute("title", "sendEmail.send.title")
        model.addAttribute("contentHeader", "sendEmail.send.header")
        return "sendTestEmail"
    }

    @PostMapping("/invite-new-user", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendEmail(
        model: Model,
        @Valid
        emailModel: ConfirmedEmailDataModel,
        result: BindingResult,
        principal: Principal,
    ): String {
        try {
            val emailAddress: String = emailModel.email
            val currentAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!
            val token = invitationService.createInvitationToken(emailAddress, currentAuthority)
            val invitationLinkAddress = invitationService.buildInvitationUri(token)
            emailSender.sendEmail(
                emailAddress,
                LocalAuthorityInvitationEmail(currentAuthority, invitationLinkAddress),
            )

            model.addAttribute("title", "sendEmail.sent.title")
            model.addAttribute("contentHeader", "sendEmail.sent.header")
            model.addAttribute("contentHeaderParams", emailAddress)
            return "index"
        } catch (retryException: TransientEmailSentException) {
            model.addAttribute("title", "sendEmail.send.title")
            model.addAttribute("contentHeader", "sendEmail.send.errorTitle")
            return "sendTestEmail"
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
