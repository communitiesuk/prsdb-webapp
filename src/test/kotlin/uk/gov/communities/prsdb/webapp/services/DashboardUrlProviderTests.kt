package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_EDITOR
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL

class DashboardUrlProviderTests {
    private val dashboardUrlProvider = DashboardUrlProvider()

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    private fun setAuthenticatedRoles(vararg roles: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("user", "password", roles.map { SimpleGrantedAuthority(it) })
    }

    @Test
    fun `returns the landlord dashboard for an individual landlord`() {
        // Arrange
        setAuthenticatedRoles(ROLE_INDIVIDUAL_LANDLORD)

        // Act, Assert
        assertEquals(LANDLORD_DASHBOARD_URL, dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `returns the landlord dashboard for an org admin`() {
        // Arrange
        setAuthenticatedRoles(ROLE_ORG_ADMIN)

        // Act, Assert
        assertEquals(LANDLORD_DASHBOARD_URL, dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `returns the landlord dashboard for an org editor`() {
        // Arrange
        setAuthenticatedRoles(ROLE_ORG_EDITOR)

        // Act, Assert
        assertEquals(LANDLORD_DASHBOARD_URL, dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `returns the local council dashboard for a standard local council user`() {
        // Arrange
        setAuthenticatedRoles(ROLE_LOCAL_COUNCIL_USER)

        // Act, Assert
        assertEquals(LOCAL_COUNCIL_DASHBOARD_URL, dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `returns the local council dashboard for a local council admin`() {
        // Arrange
        setAuthenticatedRoles(ROLE_LOCAL_COUNCIL_ADMIN, ROLE_LOCAL_COUNCIL_USER)

        // Act, Assert
        assertEquals(LOCAL_COUNCIL_DASHBOARD_URL, dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `returns the system operator dashboard for a system operator`() {
        // Arrange
        setAuthenticatedRoles(ROLE_SYSTEM_OPERATOR)

        // Act, Assert
        assertEquals(SYSTEM_OPERATOR_DASHBOARD_URL, dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `returns null when there is no authenticated user`() {
        // Act, Assert
        assertNull(dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }

    @Test
    fun `returns null when the user has no recognised role`() {
        // Arrange
        setAuthenticatedRoles("ROLE_SOMETHING_ELSE")

        // Act, Assert
        assertNull(dashboardUrlProvider.getDashboardUrlForCurrentUser())
    }
}
