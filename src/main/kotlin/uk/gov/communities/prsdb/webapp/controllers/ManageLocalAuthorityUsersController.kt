package uk.gov.communities.prsdb.webapp.controllers

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.ConfirmedEmailDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.ValidationService
import java.security.Principal
import java.util.Locale

@PreAuthorize("hasRole('LA_ADMIN')")
@Controller
@RequestMapping("/manage-users")
class ManageLocalAuthorityUsersController(
    var emailSender: EmailNotificationService<LocalAuthorityInvitationEmail>,
    var invitationService: LocalAuthorityInvitationService,
    val localAuthorityDataService: LocalAuthorityDataService,
    val validator: ValidationService,
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
    fun inviteNewUser(
        model: Model,
        principal: Principal,
        confirmedEmailDataModel: ConfirmedEmailDataModel,
    ): String {
        val currentAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!
        model.addAttribute("councilName", currentAuthority.name)
        model.addAttribute("serviceName", SERVICE_NAME)

        val dataModelJson = model.getAttribute("serializedDataModel") as String?
        if (dataModelJson is String) {
            val previousDataModel = Json.decodeFromString<ConfirmedEmailDataModel>(dataModelJson)
            val bindingResult = validator.validateDataModel(previousDataModel)

            model.addAttribute("confirmedEmailDataModel", previousDataModel)
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "confirmedEmailDataModel", bindingResult)
        }

        return "inviteLAUser"
    }

    @PostMapping("/invite-new-user", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendInvitation(
        model: Model,
        emailModel: ConfirmedEmailDataModel,
        principal: Principal,
        redirectAttributes: RedirectAttributes,
    ): String {
        model.addAttribute("serviceName", SERVICE_NAME)
        val currentAuthority = localAuthorityDataService.getLocalAuthorityForUser(principal.name)!!
        model.addAttribute("councilName", currentAuthority.name)

        val result = validator.validateDataModel(emailModel)
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                "serializedDataModel",
                Json.encodeToJsonElement(emailModel).toString(),
            )
            return "redirect:invite-new-user"
        }

        try {
            val token = invitationService.createInvitationToken(emailModel.email, currentAuthority)
            val invitationLinkAddress = invitationService.buildInvitationUri(token)
            emailSender.sendEmail(
                emailModel.email,
                LocalAuthorityInvitationEmail(currentAuthority, invitationLinkAddress),
            )

            model.addAttribute("contentHeader", "You have sent a test email to ${emailModel.email}")
            model.addAttribute("title", "Email sent")
            return "index"
        } catch (retryException: TransientEmailSentException) {
            result.reject("addLAUser.error.retryable")
            return "inviteLAUser"
        }
    }
}
