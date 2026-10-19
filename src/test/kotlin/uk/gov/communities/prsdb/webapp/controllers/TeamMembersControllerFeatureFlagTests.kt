package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole.ADMIN
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.services.ManageTeamMembersUrlProvider
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord

// TODO PDJB-1828: Delete this class when the MULTI_USER_ORGANISATIONS flag is removed
class TeamMembersControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    private lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    @MockitoBean
    private lateinit var organisationalLandlordInvitationService: OrganisationalLandlordInvitationService

    @MockitoBean
    private lateinit var manageTeamMembersUrlProvider: ManageTeamMembersUrlProvider

    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    @Test
    fun `team members page is available if the multi-user organisations feature flag is enabled`() {
        featureFlagManager.enableFeature(MULTI_USER_ORGANISATIONS)
        val organisation = createOrgLandlord()
        val currentUser = OrganisationalLandlordUser(organisation, PrsdbUser("user-123"), "Current User", "user@example.com", ADMIN)
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(organisation)
        whenever(organisationalLandlordUserService.getOrganisationalLandlordUsers(organisation)).thenReturn(listOf(currentUser))

        mvc
            .get(TEAM_MEMBERS_ROUTE)
            .andExpect { status { isOk() } }
    }

    // TODO PDJB-1828: Delete this nested class when the MULTI_USER_ORGANISATIONS flag is removed
    @Nested
    inner class BeforePdjb1210MultiUserOrganisations {
        @BeforeEach
        fun disableMultiUserOrganisations() {
            featureFlagManager.disableFeature(MULTI_USER_ORGANISATIONS)
        }

        @WithMockUser(roles = ["ORG_ADMIN"])
        @Test
        fun `team members page is unavailable if the multi-user organisations feature flag is disabled`() {
            mvc
                .get(TEAM_MEMBERS_ROUTE)
                .andExpect { status { isNotFound() } }
        }

        @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
        @Test
        fun `removing an expired invitation is unavailable if the multi-user organisations feature flag is disabled`() {
            mvc
                .get(TeamMembersController.getRemoveExpiredInvitationPath(1))
                .andExpect { status { isNotFound() } }
        }
    }
}
