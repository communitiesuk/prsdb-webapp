package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.ACT_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ACT_AS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.STOP_ACTING_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.ACTING_AS_LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_SYSTEM_OPERATOR_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService
import java.util.Optional

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
class ActingAsLandlordController(
    private val actingAsLandlordService: ActingAsLandlordService,
) {
    @AvailableWhenFeatureEnabled(ACT_AS_LANDLORD)
    @PostMapping(ACT_AS_LANDLORD_ROUTE)
    fun startActingAsLandlord(
        @PathVariable id: Long,
        @RequestParam(required = false) memberId: Long?,
    ): String {
        actingAsLandlordService.startActingAsLandlord(id, memberId)
        return "redirect:$ACTING_AS_LANDLORD_DASHBOARD_URL"
    }

    @AvailableWhenFeatureEnabled(ACT_AS_LANDLORD)
    @PostMapping(STOP_ACTING_ROUTE)
    fun stopActing(): String {
        actingAsLandlordService.stopActing()
        return "redirect:$SYSTEM_OPERATOR_DASHBOARD_URL"
    }

    companion object {
        const val ACT_AS_LANDLORD_ROUTE = "$LANDLORD_DETAILS_FOR_SYSTEM_OPERATOR_ROUTE/$ACT_AS_PATH_SEGMENT"
        const val STOP_ACTING_ROUTE = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$STOP_ACTING_PATH_SEGMENT"

        fun getActAsLandlordPath(
            landlordId: Long,
            memberId: Long? = null,
        ): String =
            UriComponentsBuilder
                .fromUriString(UriTemplate(ACT_AS_LANDLORD_ROUTE).expand(mapOf("id" to landlordId)).toASCIIString())
                .queryParamIfPresent("memberId", Optional.ofNullable(memberId))
                .build()
                .toUriString()
    }
}
