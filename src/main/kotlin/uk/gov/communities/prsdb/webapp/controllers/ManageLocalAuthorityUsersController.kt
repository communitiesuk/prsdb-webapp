@file:Suppress("ktlint:standard:no-wildcard-imports")

package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.*
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.ConfirmedEmailDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import java.security.Principal
import java.util.Locale

@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/manage-users")
class ManageLocalAuthorityUsersController(
    var emailSender: EmailNotificationService<LocalAuthorityInvitationEmail>,
    var invitationService: LocalAuthorityInvitationService,
    val localAuthorityDataService: LocalAuthorityDataService,
    @Qualifier("messageSource") private val messageSource: MessageSource,
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

    @GetMapping("/invite-new-user")
    fun exampleEmailPage(
        model: Model,
        principal: Principal,
    ): String {
        val currentAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!
        model.addAttribute("councilName", currentAuthority.name)
        model.addAttribute("councilEmail", currentAuthority.name + ".co.uk")
        model.addAttribute("serviceName", SERVICE_NAME)
        return "inviteLAUser"
    }

    @PostMapping("/invite-new-user", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendEmail(
        model: Model,
        @Valid
        emailModel: ConfirmedEmailDataModel,
        result: BindingResult,
        principal: Principal,
    ): String {
        model.addAttribute("serviceName", SERVICE_NAME)
        val currentAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!
        if (result.hasErrors()) {
            model.addAttribute("councilName", currentAuthority.name)
            model.addAttribute("councilEmail", currentAuthority.name + ".co.uk")
            model.addAttribute(
                "validationErrors",
                getValidationErrors(emailModel, result),
            )
            return "inviteLAUser"
        }
        model.addAttribute("validationErrors", InviteUserValidationState(emailModel.email, emailModel.confirmEmail))

        val emailAddress: String = emailModel.email
        try {
            val token = invitationService.createInvitationToken(emailAddress, currentAuthority)
            val invitationLinkAddress = invitationService.buildInvitationUri(token)
            emailSender.sendEmail(
                emailAddress,
                LocalAuthorityInvitationEmail(currentAuthority, invitationLinkAddress),
            )

            model.addAttribute("contentHeader", "You have sent a test email to $emailAddress")
            model.addAttribute("title", "Email sent")
            return "index"
        } catch (retryException: TransientEmailSentException) {
            model.addAttribute("councilName", currentAuthority.name)
            model.addAttribute("councilEmail", currentAuthority.name + ".co.uk")
            return "inviteLAUser"
        }
    }

    private fun getValidationErrors(
        model: ConfirmedEmailDataModel,
        result: BindingResult,
    ): InviteUserValidationState =
        InviteUserValidationState(
            if (result.allErrors
                    .map { it.defaultMessage }
                    .contains(ConfirmedEmailDataModel.NO_EMAIL_ERROR_MESSAGE)
            ) {
                FieldValidation(model.email, ADD_LA_USER_MISSING_EMAIL)
            } else if (result.allErrors
                    .map { it.defaultMessage }
                    .contains(ConfirmedEmailDataModel.NOT_AN_EMAIL_ERROR_MESSAGE)
            ) {
                FieldValidation(model.email, ADD_LA_USER_INVALID_EMAIL)
            } else {
                FieldValidation(model.email)
            },
            if (result.allErrors
                    .map { it.defaultMessage }
                    .contains(ConfirmedEmailDataModel.NO_CONFIRMATION_ERROR_MESSAGE)
            ) {
                FieldValidation(model.confirmEmail, ADD_LA_USER_MISSING_CONFIRMATION)
            } else if (result.allErrors
                    .map { it.defaultMessage }
                    .contains(
                        ConfirmedEmailDataModel.CONFIRMATION_DOES_NOT_MATCH_EMAIL_ERROR_MESSAGE,
                    )
            ) {
                FieldValidation(model.confirmEmail, ADD_LA_USER_NON_MATCHING_CONFIRMATION)
            } else {
                FieldValidation(model.confirmEmail)
            },
        )
}

class InviteUserValidationState(
    val emailValidation: FieldValidation,
    val confirmEmailValidation: FieldValidation,
) {
    constructor(
        validEmail: String,
        validConfirmEmail: String,
    ) : this(FieldValidation(validEmail), FieldValidation(validConfirmEmail))

    val hasErrors: Boolean = emailValidation.hasError || confirmEmailValidation.hasError
}

class FieldValidation(
    val value: String,
    val messageKey: String? = null,
) {
    val hasError: Boolean = messageKey != null
}
