package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.services.TeamMembersUrlProvider

class TeamMembersUrlProviderTests : FeatureFlagTest() {
    @Autowired
    lateinit var teamMembersUrlProvider: TeamMembersUrlProvider

    @MockitoBean
    lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

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
    fun `returns the team members route for an organisation landlord user`() {
        setAuthenticatedUser("org-user", ROLE_INDIVIDUAL_LANDLORD)
        whenever(organisationalLandlordUserRepository.existsByBaseUser_Id("org-user")).thenReturn(true)

        assertEquals(TEAM_MEMBERS_ROUTE, teamMembersUrlProvider.getTeamMembersUrlForCurrentUser())
    }

    @Test
    fun `returns null for a landlord who is not an organisation landlord user`() {
        setAuthenticatedUser("individual-user", ROLE_INDIVIDUAL_LANDLORD)
        whenever(organisationalLandlordUserRepository.existsByBaseUser_Id("individual-user")).thenReturn(false)

        assertNull(teamMembersUrlProvider.getTeamMembersUrlForCurrentUser())
    }

    @Test
    fun `returns null when the user is not a landlord`() {
        setAuthenticatedUser("local-council-user", ROLE_LOCAL_COUNCIL_USER)

        assertNull(teamMembersUrlProvider.getTeamMembersUrlForCurrentUser())
        verify(organisationalLandlordUserRepository, never()).existsByBaseUser_Id(any())
    }

    @Test
    fun `returns null when there is no authenticated user`() {
        assertNull(teamMembersUrlProvider.getTeamMembersUrlForCurrentUser())
    }

    // TODO PDJB-1828: Remove this test when the MULTI_USER_ORGANISATIONS flag is removed
    @Test
    fun `when feature is disabled returns null even for an organisation landlord user`() {
        featureFlagManager.disableFeature(MULTI_USER_ORGANISATIONS)
        setAuthenticatedUser("org-user", ROLE_INDIVIDUAL_LANDLORD)
        whenever(organisationalLandlordUserRepository.existsByBaseUser_Id("org-user")).thenReturn(true)

        assertNull(teamMembersUrlProvider.getTeamMembersUrlForCurrentUser())
        verify(organisationalLandlordUserRepository, never()).existsByBaseUser_Id(any())
    }
}
