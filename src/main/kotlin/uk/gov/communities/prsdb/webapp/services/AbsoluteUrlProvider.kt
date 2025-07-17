package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import java.net.URI

@Service
class AbsoluteUrlProvider {
    @Value("\${base-url.landlord}")
    private lateinit var landlordBaseUrl: String

    @Value("\${base-url.local-authority}")
    private lateinit var localAuthorityBaseUrl: String

    fun buildLandlordDashboardUri(): URI = uriFromMethodCall(on(LandlordController::class.java).index())

    fun buildInvitationUri(token: String): URI = uriFromMethodCall(on(RegisterLAUserController::class.java).acceptInvitation(token))

    fun buildComplianceInformationUri(propertyOwnershipId: Long): URI =
        uriFromMethodCall(on(PropertyDetailsController::class.java).getPropertyDetails(propertyOwnershipId))

    private fun uriFromMethodCall(info: Any): URI {
        val methodCallUriComponents =
            MvcUriComponentsBuilder
                .fromMethodCall(
                    UriComponentsBuilder.newInstance(),
                    info,
                ).build()

        val baseUrl =
            when (methodCallUriComponents.pathSegments[0]) {
                LANDLORD_PATH_SEGMENT -> landlordBaseUrl
                LOCAL_AUTHORITY_PATH_SEGMENT -> localAuthorityBaseUrl
                else -> throw IllegalArgumentException("Unknown base URL for path: ${methodCallUriComponents.path}")
            }

        return UriComponentsBuilder
            .fromUriString(baseUrl)
            .pathSegment(*methodCallUriComponents.pathSegments.drop(1).toTypedArray())
            .query(methodCallUriComponents.query)
            .fragment(methodCallUriComponents.fragment)
            .build()
            .toUri()
    }
}
