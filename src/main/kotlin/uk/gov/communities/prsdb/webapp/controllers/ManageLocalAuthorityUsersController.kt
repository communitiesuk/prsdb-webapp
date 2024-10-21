package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
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
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.ConfirmedEmailDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import java.security.Principal

@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/manage-users")
class ManageLocalAuthorityUsersController(
    var emailSender: EmailNotificationService<LocalAuthorityInvitationEmail>,
    var invitationService: LocalAuthorityInvitationService,
    val localAuthorityDataService: LocalAuthorityDataService,
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

        model.addAttribute("localAuthority", currentUserLocalAuthority.name)
        model.addAttribute("userList", users)
        model.addAttribute(
            "tableColumnHeadings",
            listOf(
                "manageLAUsers.table.column1Heading",
                "manageLAUsers.table.column2Heading",
                "manageLAUsers.table.column3Heading",
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
