package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService

// TODO PRSD-404: Remove this controller once there is another way to reach the EmailNotificationService
// This controller is an example controller to demo our integration with notify and should be removed once
// there is an integration that belongs to an intended releasable feature.
@Controller
class ExampleEmailSendingController(
    var invitationService: LocalAuthorityInvitationService,
) {
    @GetMapping("/invitation")
    fun magicLink(
        model: Model,
        token: String,
    ): ModelAndView {
        val authority = invitationService.getAuthorityForToken(token)
        model.addAttribute("contentHeader", "The local authority issuing that token was: ${authority.name}")
        model.addAttribute("title", "Magic link")
        model.addAttribute("serviceName", SERVICE_NAME)
        return ModelAndView("index", "index", model)
    }
}
