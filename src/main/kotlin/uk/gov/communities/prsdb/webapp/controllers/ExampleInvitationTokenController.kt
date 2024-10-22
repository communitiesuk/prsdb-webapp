package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.SERVICE_NAME
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService

// TODO PRSD-405: Remove this controller once there is another way to use invitation tokens
// This controller is an example controller to demo invitation tokens working and should be removed once
// there is a page that belongs to an intended releasable feature.
@Controller
class ExampleInvitationTokenController(
    var invitationService: LocalAuthorityInvitationService,
) {
    @GetMapping("/invitation")
    fun acceptInvitation(
        model: Model,
        token: String,
    ): ModelAndView {
        val authority = invitationService.getAuthorityForToken(token)
        model.addAttribute("contentHeader", "The local authority issuing that token was: ${authority.name}")
        model.addAttribute("title", "Magic link")
        model.addAttribute("serviceName", SERVICE_NAME)

        // TODO PRSD-405: This is using a ModelAndView instead of returning a string to handle an error that otherwise occurs in
        // the LocalAuthorityInvitationService method that creates the invitation url using MvcUriComponentsBuilder.fromMethodName
        // see https://stackoverflow.com/questions/28580281/using-mvcuricomponentsbuilderfrommethodcall-with-string-as-the-return-type for details
        return ModelAndView("index", "index", model)
    }
}
