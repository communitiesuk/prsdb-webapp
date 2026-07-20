package uk.gov.communities.prsdb.webapp.services

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DASHBOARD_NAV_LINK
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL

@PrsdbWebService
class DashboardUrlProvider(
    private val featureFlagManager: FeatureFlagManager,
) {
    fun getDashboardUrlForCurrentUser(): String? {
        if (!featureFlagManager.checkFeature(DASHBOARD_NAV_LINK)) {
            return null
        }

        val authorities =
            SecurityContextHolder.getContext().authentication
                ?.authorities
                ?.map { it.authority }
                ?: return null

        return when {
            authorities.contains(ROLE_LANDLORD) -> LANDLORD_DASHBOARD_URL
            authorities.contains(ROLE_LOCAL_COUNCIL_USER) || authorities.contains(ROLE_LOCAL_COUNCIL_ADMIN) ->
                LOCAL_COUNCIL_DASHBOARD_URL
            authorities.contains(ROLE_SYSTEM_OPERATOR) -> SYSTEM_OPERATOR_DASHBOARD_URL
            else -> null
        }
    }
}
