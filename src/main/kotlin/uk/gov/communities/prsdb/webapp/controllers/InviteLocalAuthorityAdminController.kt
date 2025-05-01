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
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteLocalAuthorityAdminFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@Controller
@RequestMapping("/$SYSTEM_OPERATOR_PATH_SEGMENT")
class InviteLocalAuthorityAdminController(
    private val localAuthorityService: LocalAuthorityService,
) {
    @GetMapping("/invite-la-admin")
    fun inviteLocalAuthorityAdmin(model: Model): String {
        addSelectOptionsToModel(model)
        model.addAttribute("inviteLocalAuthorityAdminFormModel", InviteLocalAuthorityAdminFormModel())

        return "inviteLocalAuthorityAdminUser"
    }

    @PostMapping("/invite-la-admin", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun sendInvitation(
        model: Model,
        @Valid
        @ModelAttribute
        inviteLocalAuthorityAdminFormModel: InviteLocalAuthorityAdminFormModel,
        bindingResult: BindingResult,
    ): String {
        if (bindingResult.hasErrors()) {
            addSelectOptionsToModel(model)
            return "inviteLocalAuthorityAdminUser"
        }

        return "redirect:/$SYSTEM_OPERATOR_PATH_SEGMENT/invite-la-admin/success"
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

    @GetMapping("/invite-la-admin/success")
    fun confirmation(): String = "inviteLocalAuthorityAdminSuccess"
}
