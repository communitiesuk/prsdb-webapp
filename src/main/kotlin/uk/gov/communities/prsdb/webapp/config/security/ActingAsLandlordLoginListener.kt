package uk.gov.communities.prsdb.webapp.config.security

import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService

// Session fixation protection carries session attributes over to the new session on sign in,
// so without this a system operator could start a fresh login still acting as a landlord.
@PrsdbWebComponent
class ActingAsLandlordLoginListener(
    private val actingAsLandlordService: ActingAsLandlordService,
) : ApplicationListener<AuthenticationSuccessEvent> {
    override fun onApplicationEvent(event: AuthenticationSuccessEvent) {
        actingAsLandlordService.clearActingAsUserId()
    }
}
