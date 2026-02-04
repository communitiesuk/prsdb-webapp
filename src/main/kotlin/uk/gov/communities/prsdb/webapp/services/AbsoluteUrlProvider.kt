package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import java.net.URI

@Service
class AbsoluteUrlProvider {
    @Value("\${base-url.landlord}")
    private lateinit var landlordBaseUrl: String

    @Value("\${base-url.local-council}")
    private lateinit var localCouncilBaseUrl: String

    fun buildLandlordDashboardUri(): URI = uriFromMethodCall(on(LandlordController::class.java).index())

    fun buildLocalCouncilDashboardUri(): URI = uriFromMethodCall(on(LocalCouncilDashboardController::class.java).index())

    fun buildInvitationUri(token: String): URI =
        uriFromMethodCall(on(RegisterLocalCouncilUserController::class.java).acceptInvitation(token))

    fun buildJointLandlordInvitationUri(token: String): URI =
        UriComponentsBuilder
            .fromUriString(landlordBaseUrl)
            .pathSegment(JOINT_LANDLORD_INVITATION_PATH_SEGMENT, token)
            .build()
            .toUri()

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
                LOCAL_COUNCIL_PATH_SEGMENT -> localCouncilBaseUrl
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
