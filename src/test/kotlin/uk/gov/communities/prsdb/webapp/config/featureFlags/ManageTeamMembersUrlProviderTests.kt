package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.services.ManageTeamMembersUrlProvider
import uk.gov.communities.prsdb.webapp.services.OrganisationPermissionsProvider

class ManageTeamMembersUrlProviderTests : FeatureFlagTest() {
    @Autowired
    lateinit var manageTeamMembersUrlProvider: ManageTeamMembersUrlProvider

    @MockitoBean
    lateinit var organisationPermissionsProvider: OrganisationPermissionsProvider

    @BeforeEach
    fun enableFeatureFlags() {
        featureFlagManager.enableFeature(MULTI_USER_ORGANISATIONS)
    }

    @Test
    fun `returns the team members route when the current user is an organisation admin`() {
        whenever(organisationPermissionsProvider.isCurrentUserOrgAdmin()).thenReturn(true)

        assertEquals(TEAM_MEMBERS_ROUTE, manageTeamMembersUrlProvider.getManageTeamMembersUrlForCurrentUser())
    }

    @Test
    fun `returns null when the current user is not an organisation admin`() {
        whenever(organisationPermissionsProvider.isCurrentUserOrgAdmin()).thenReturn(false)

        assertNull(manageTeamMembersUrlProvider.getManageTeamMembersUrlForCurrentUser())
    }

    // TODO PDJB-1828: Remove this test when the MULTI_USER_ORGANISATIONS flag is removed
    @Test
    fun `when feature is disabled returns null even for an organisation admin`() {
        featureFlagManager.disableFeature(MULTI_USER_ORGANISATIONS)
        whenever(organisationPermissionsProvider.isCurrentUserOrgAdmin()).thenReturn(true)

        assertNull(manageTeamMembersUrlProvider.getManageTeamMembersUrlForCurrentUser())
    }
}
