package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController
import uk.gov.communities.prsdb.webapp.controllers.NoLongerALandlordController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.journeys.noLongerALandlord.stepConfig.ConfirmStep
import java.net.URI
import java.security.Principal

@Service
class AbsoluteUrlProvider(
    @Value("\${base-url.landlord}") landlordBaseUrl: String,
    @Value("\${base-url.local-council}") localCouncilBaseUrl: String,
) {
    private val landlordBaseUrl: String = ensureScheme(landlordBaseUrl)
    private val localCouncilBaseUrl: String = ensureScheme(localCouncilBaseUrl)

    private companion object {
        private fun ensureScheme(baseUrl: String): String =
            if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) baseUrl else "https://$baseUrl"
    }

    fun buildLandlordDashboardUri(): URI = uriFromMethodCall(on(LandlordController::class.java).index())

    fun buildLocalCouncilDashboardUri(): URI = uriFromMethodCall(on(LocalCouncilDashboardController::class.java).index())

    fun buildInvitationUri(
        token: String,
        principal: Principal,
    ): URI = uriFromMethodCall(on(RegisterLocalCouncilUserController::class.java).acceptInvitation(token, principal))

    fun buildJointLandlordInvitationUri(token: String): URI =
        uriFromMethodCall(on(AcceptOrRejectJointLandlordInvitationController::class.java).startJourney(token))

    fun buildComplianceInformationUri(propertyOwnershipId: Long): URI {
        val baseUri = buildPropertyDetailsUri(propertyOwnershipId)
        return UriComponentsBuilder
            .fromUri(baseUri)
            .fragment(COMPLIANCE_INFO_FRAGMENT)
            .build()
            .toUri()
    }

    fun buildPropertyDetailsUri(propertyOwnershipId: Long): URI =
        uriFromMethodCall(on(PropertyDetailsController::class.java).getPropertyDetails(propertyOwnershipId))

    fun buildLeavePropertyUri(propertyOwnershipId: Long): URI =
        uriFromMethodCall(
            on(NoLongerALandlordController::class.java)
                .getJourneyStep(ConfirmStep.ROUTE_SEGMENT, propertyOwnershipId) { "PrincipalName" },
        )

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
