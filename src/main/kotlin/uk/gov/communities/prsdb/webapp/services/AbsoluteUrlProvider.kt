package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on
import uk.gov.communities.prsdb.webapp.controllers.LandlordDashboardController
import java.net.URI

@Service
class AbsoluteUrlProvider {
    fun buildLandlordDashboardUri(): URI =
        MvcUriComponentsBuilder
            .fromMethodCall(on(LandlordDashboardController::class.java).index())
            .build()
            .toUri()
}
