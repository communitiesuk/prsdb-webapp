package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
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
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.ConfirmedEmailDataModel
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
        val totalPages = ceil((totalUsers.toDouble() / MAX_ENTRIES_IN_TABLE_PAGE.toDouble())).toInt()

        val shouldPaginate = totalPages > 1
        val currentPageNumber = getCurrentPage(page)

        val pagedUserList =
            localAuthorityDataService.getUserList(
                currentUserLocalAuthority,
                currentPageNumber,
                nActiveUsers,
                shouldPaginate,
            )

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
