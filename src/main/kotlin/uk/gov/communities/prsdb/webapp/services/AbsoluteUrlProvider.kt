package uk.gov.communities.prsdb.webapp.services

import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import java.net.URI

@PrsdbWebService
class AbsoluteUrlProvider {
    fun buildLandlordDashboardUri(): URI =
        MvcUriComponentsBuilder
            .fromMethodCall(on(LandlordController::class.java).index())
            .build()
            .toUri()

    fun buildInvitationUri(token: String): URI =
        MvcUriComponentsBuilder
            .fromMethodCall(on(RegisterLAUserController::class.java).acceptInvitation(token))
            .build()
            .toUri()
}
