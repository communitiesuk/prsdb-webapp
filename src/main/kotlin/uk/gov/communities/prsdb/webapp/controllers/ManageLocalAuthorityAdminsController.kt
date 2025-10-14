package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INVITE_LA_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MANAGE_ADMIN_USERS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityAdminsController.Companion.SYSTEM_OPERATOR_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.requestModels.InviteLocalAuthorityAdminModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PaginationViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityAdminInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import java.security.Principal

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(SYSTEM_OPERATOR_ROUTE)
class ManageLocalAuthorityAdminsController(
    private val localAuthorityService: LocalAuthorityService,
    private val localAuthorityDataService: LocalAuthorityDataService,
    private val invitationEmailSender: EmailNotificationService<LocalAuthorityAdminInvitationEmail>,
    private val invitationService: LocalAuthorityInvitationService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) {
    @GetMapping("/$INVITE_LA_ADMIN_PATH_SEGMENT")
    fun inviteLocalAuthorityAdmin(model: Model): String {
        addSelectOptionsToModel(model)
        model.addAttribute("inviteLocalAuthorityAdminModel", InviteLocalAuthorityAdminModel())

        return "inviteLocalAuthorityAdminUser"
    }

    @PostMapping("/$INVITE_LA_ADMIN_PATH_SEGMENT", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendInvitation(
        model: Model,
        @Valid
        @ModelAttribute
        inviteLocalAuthorityAdminModel: InviteLocalAuthorityAdminModel,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (bindingResult.hasErrors()) {
            addSelectOptionsToModel(model)
            return "inviteLocalAuthorityAdminUser"
        }

        try {
            val localAuthority = localAuthorityService.retrieveLocalAuthorityById(inviteLocalAuthorityAdminModel.localAuthorityId!!)

            val token =
                invitationService.createInvitationToken(
                    inviteLocalAuthorityAdminModel.email,
                    localAuthority,
                    invitedAsAdmin = true,
                )
            val invitationLinkAddress = absoluteUrlProvider.buildInvitationUri(token)
            invitationEmailSender.sendEmail(
                inviteLocalAuthorityAdminModel.email,
                LocalAuthorityAdminInvitationEmail(localAuthority, invitationLinkAddress),
            )

            redirectAttributes.addFlashAttribute("invitedEmailAddress", inviteLocalAuthorityAdminModel.email)
            redirectAttributes.addFlashAttribute("localAuthorityName", localAuthority.name)
            return "redirect:$INVITE_LA_ADMIN_CONFIRMATION_ROUTE"
        } catch (retryException: TransientEmailSentException) {
            bindingResult.reject("addLAUser.error.retryable")
            return "inviteLocalAuthorityAdminUser"
        }
    }

    private fun addSelectOptionsToModel(model: Model) {
        val localAuthoritiesSelectOptions =
            localAuthorityService.retrieveAllLocalAuthorities().map {
                SelectViewModel(
                    value = it.id,
                    label = it.name,
                )
            }
        model.addAttribute("selectOptions", localAuthoritiesSelectOptions)
    }

    @GetMapping("/$INVITE_LA_ADMIN_PATH_SEGMENT/$CONFIRMATION_PATH_SEGMENT")
    fun confirmation(model: Model): String {
        if (model.getAttribute("invitedEmailAddress") == null || model.getAttribute("localAuthorityName") == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing attributes, has the user navigated directly to this page?")
        }

        model.addAttribute("inviteAnotherUserUrl", INVITE_LA_ADMIN_ROUTE)
        // TODO PRSD-672: Add link to the system operator dashboard
        // TODO PRSD-1351 add return to Manage Admins button here?
        model.addAttribute("dashboardUrl", "#")

        return "inviteLocalAuthorityAdminConfirmation"
    }

    @GetMapping("/$MANAGE_ADMIN_USERS_PATH_SEGMENT")
    fun manageAdminUsers(
        model: Model,
        principal: Principal,
        @RequestParam(value = "page", required = false) @Min(1) page: Int = 1,
        request: HttpServletRequest,
    ): String {
        val pagedUserList =
            localAuthorityDataService.getPaginatedAdminUsersAndInvitations(page - 1)

        if (pagedUserList.totalPages != 0 && pagedUserList.totalPages < page) {
            return "redirect:$MANAGE_LA_ADMINS_ROUTE"
        }

        model.addAttribute("userList", pagedUserList)
        model.addAttribute(
            "paginationViewModel",
            PaginationViewModel(page, pagedUserList.totalPages, request),
        )
        model.addAttribute("inviteAdminsUrl", INVITE_LA_ADMIN_ROUTE)
        return "manageLocalAuthorityAdminUsers"
    }

    companion object {
        const val SYSTEM_OPERATOR_ROUTE = "/$LOCAL_AUTHORITY_PATH_SEGMENT/$SYSTEM_OPERATOR_PATH_SEGMENT"
        const val INVITE_LA_ADMIN_ROUTE = "$SYSTEM_OPERATOR_ROUTE/$INVITE_LA_ADMIN_PATH_SEGMENT"
        const val MANAGE_LA_ADMINS_ROUTE = "$SYSTEM_OPERATOR_ROUTE/$MANAGE_ADMIN_USERS_PATH_SEGMENT"

        const val INVITE_LA_ADMIN_CONFIRMATION_ROUTE = "$INVITE_LA_ADMIN_ROUTE/$CONFIRMATION_PATH_SEGMENT"
    }
}
