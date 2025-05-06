package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INVITE_LA_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.models.requestModels.InviteLocalAuthorityAdminModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@Controller
@RequestMapping("/$SYSTEM_OPERATOR_PATH_SEGMENT")
class InviteLocalAuthorityAdminController(
    private val localAuthorityService: LocalAuthorityService,
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
    ): String {
        if (bindingResult.hasErrors()) {
            addSelectOptionsToModel(model)
            return "inviteLocalAuthorityAdminUser"
        }

        // TODO: PRSD-1096 send invitation email
        return "redirect:/$SYSTEM_OPERATOR_PATH_SEGMENT/$INVITE_LA_ADMIN_PATH_SEGMENT/$CONFIRMATION_PATH_SEGMENT"
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
    fun confirmation(): String = "inviteLocalAuthorityAdminSuccess"

    companion object {
        const val INVITE_LA_ADMIN_ROUTE = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$INVITE_LA_ADMIN_PATH_SEGMENT"
    }
}
