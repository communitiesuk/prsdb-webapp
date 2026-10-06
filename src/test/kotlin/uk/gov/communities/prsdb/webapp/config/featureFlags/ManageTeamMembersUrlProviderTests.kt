package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_ADMIN
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.services.ManageTeamMembersUrlProvider

class ManageTeamMembersUrlProviderTests : FeatureFlagTest() {
    @Autowired
    lateinit var manageTeamMembersUrlProvider: ManageTeamMembersUrlProvider

    @BeforeEach
    fun enableFeatureFlags() {
        featureFlagManager.enableFeature(MULTI_USER_ORGANISATIONS)
    }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    private fun setAuthenticatedUser(
        userId: String,
        vararg roles: String,
    ) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(userId, "password", roles.map { SimpleGrantedAuthority(it) })
    }

    @Test
    fun `returns the team members route for an organisation admin`() {
        setAuthenticatedUser("org-admin", ROLE_ORG_ADMIN)

        assertEquals(TEAM_MEMBERS_ROUTE, manageTeamMembersUrlProvider.getManageTeamMembersUrlForCurrentUser())
    }

    @Test
    fun `returns null for a landlord who is not an organisation admin`() {
        setAuthenticatedUser("individual-user", ROLE_INDIVIDUAL_LANDLORD)

        assertNull(manageTeamMembersUrlProvider.getManageTeamMembersUrlForCurrentUser())
    }

    @Test
    fun `returns null when the user is not a landlord`() {
        setAuthenticatedUser("local-council-user", ROLE_LOCAL_COUNCIL_USER)

        assertNull(manageTeamMembersUrlProvider.getManageTeamMembersUrlForCurrentUser())
    }

    @Test
    fun `returns null when there is no authenticated user`() {
        assertNull(manageTeamMembersUrlProvider.getManageTeamMembersUrlForCurrentUser())
    }

    // TODO PDJB-1828: Remove this test when the MULTI_USER_ORGANISATIONS flag is removed
    @Test
    fun `when feature is disabled returns null even for an organisation admin`() {
        featureFlagManager.disableFeature(MULTI_USER_ORGANISATIONS)
        setAuthenticatedUser("org-admin", ROLE_ORG_ADMIN)

        assertNull(manageTeamMembersUrlProvider.getManageTeamMembersUrlForCurrentUser())
    }
}
