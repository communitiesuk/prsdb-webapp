package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.DASHBOARD_NAV_LINK
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL

@PrsdbFlip(name = DASHBOARD_NAV_LINK, alterBean = "dashboard-url-provider-flag-on")
interface DashboardUrlProvider {
    fun getDashboardUrlForCurrentUser(): String?
}

@Primary
@PrsdbWebService("dashboard-url-provider-flag-off")
class DashboardUrlProviderImplFlagOff : DashboardUrlProvider {
    // The dashboard nav link feature is disabled, so no dashboard URL is provided.
    override fun getDashboardUrlForCurrentUser(): String? = null
}

@PrsdbWebService("dashboard-url-provider-flag-on")
class DashboardUrlProviderImplFlagOn : DashboardUrlProvider {
    override fun getDashboardUrlForCurrentUser(): String? {
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
