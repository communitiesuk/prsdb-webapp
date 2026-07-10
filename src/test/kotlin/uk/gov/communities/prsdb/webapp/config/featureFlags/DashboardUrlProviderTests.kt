package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.constants.DASHBOARD_NAV_LINK
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.DashboardUrlProvider

class DashboardUrlProviderTests : FeatureFlagTest() {
    @Autowired
    lateinit var dashboardUrlProvider: DashboardUrlProvider

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    private fun setAuthenticatedRoles(vararg roles: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("user", "password", roles.map { SimpleGrantedAuthority(it) })
    }

    @Test
    fun `when feature is enabled returns the landlord dashboard for a landlord`() {
        featureFlagManager.enableFeature(DASHBOARD_NAV_LINK)
        setAuthenticatedRoles(ROLE_LANDLORD)

        assertEquals(LANDLORD_DASHBOARD_URL, dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `when feature is enabled returns the local council dashboard for a standard local council user`() {
        featureFlagManager.enableFeature(DASHBOARD_NAV_LINK)
        setAuthenticatedRoles(ROLE_LOCAL_COUNCIL_USER)

        assertEquals(LOCAL_COUNCIL_DASHBOARD_URL, dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `when feature is enabled returns the local council dashboard for a local council admin`() {
        featureFlagManager.enableFeature(DASHBOARD_NAV_LINK)
        setAuthenticatedRoles(ROLE_LOCAL_COUNCIL_ADMIN, ROLE_LOCAL_COUNCIL_USER)

        assertEquals(LOCAL_COUNCIL_DASHBOARD_URL, dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `when feature is enabled returns the system operator dashboard for a system operator`() {
        featureFlagManager.enableFeature(DASHBOARD_NAV_LINK)
        setAuthenticatedRoles(ROLE_SYSTEM_OPERATOR)

        assertEquals(SYSTEM_OPERATOR_DASHBOARD_URL, dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `when feature is enabled returns null when there is no authenticated user`() {
        featureFlagManager.enableFeature(DASHBOARD_NAV_LINK)

        assertNull(dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `when feature is enabled returns null when the user has no recognised role`() {
        featureFlagManager.enableFeature(DASHBOARD_NAV_LINK)
        setAuthenticatedRoles("ROLE_SOMETHING_ELSE")

        assertNull(dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `when feature is disabled returns null even for a landlord`() {
        featureFlagManager.disableFeature(DASHBOARD_NAV_LINK)
        setAuthenticatedRoles(ROLE_LANDLORD)

        assertNull(dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }
}
