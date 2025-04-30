package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
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
        val localAuthoritiesSelectOptions =
            localAuthorityService.retrieveAllLocalAuthorities().map {
                SelectViewModel(
                    value = it.id,
                    label = it.name,
                )
            }

        model.addAttribute("title", "forms.inviteLaAdminUser.title")
        model.addAttribute("formModel", InviteLocalAuthorityAdminFormModel())
        model.addAttribute("fieldSetHeading", "forms.inviteLaAdminUser.fieldSetHeading")
        model.addAttribute("fieldSetHint", "forms.inviteLaAdminUser.fieldSetHint")
        model.addAttribute("selectOptions", localAuthoritiesSelectOptions)

        return "inviteLocalAuthorityAdminUser"
    }
}
