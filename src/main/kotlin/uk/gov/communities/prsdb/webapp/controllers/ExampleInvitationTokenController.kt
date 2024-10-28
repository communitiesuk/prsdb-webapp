package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
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
    ): CharSequence {
        val authority = invitationService.getAuthorityForToken(token)
        model.addAttribute("title", "invitation.title")
        model.addAttribute("contentHeader", "invitation.header")
        model.addAttribute("contentHeaderParams", authority.name)

        // TODO PRSD-405: This is using a CharSequence instead of returning a String to handle an error that otherwise occurs in
        // the LocalAuthorityInvitationService method that creates the invitation url using MvcUriComponentsBuilder.fromMethodName
        // see https://github.com/spring-projects/spring-hateoas/issues/155 for details
        return "index"
    }
}
