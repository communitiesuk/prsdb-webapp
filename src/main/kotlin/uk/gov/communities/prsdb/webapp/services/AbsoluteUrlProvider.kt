package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import java.net.URI

@Service
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

    fun buildComplianceInformationUri(propertyOwnershipId: Long): URI =
        MvcUriComponentsBuilder
            .fromMethodCall(on(PropertyDetailsController::class.java).getPropertyDetails(propertyOwnershipId))
            .fragment(COMPLIANCE_INFO_FRAGMENT)
            .build()
            .toUri()
}
