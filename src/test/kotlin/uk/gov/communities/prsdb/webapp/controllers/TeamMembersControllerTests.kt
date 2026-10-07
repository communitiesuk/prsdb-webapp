package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole.ADMIN
import uk.gov.communities.prsdb.webapp.controllers.InviteTeamMemberController.Companion.INVITE_TEAM_MEMBER_START_PATH
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord

@WebMvcTest(TeamMembersController::class)
class TeamMembersControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    private lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    private fun stubOrgLandlordWithCurrentUser() {
        val organisation = createOrgLandlord()
        val currentUser = OrganisationalLandlordUser(organisation, PrsdbUser("user-123"), "Current User", "user@example.com", ADMIN)
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(organisation)
        whenever(organisationalLandlordUserService.getOrganisationalLandlordUsers(organisation)).thenReturn(listOf(currentUser))
    }

    @Test
    fun `getTeamMembers returns a redirect for unauthenticated user`() {
        mvc
            .get(TEAM_MEMBERS_ROUTE)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getTeamMembers returns 403 for an unauthorised user`() {
        mvc
            .get(TEAM_MEMBERS_ROUTE)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getTeamMembers returns 403 for a non-organisation landlord`() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())

        mvc
            .get(TEAM_MEMBERS_ROUTE)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getTeamMembers returns 200 for an organisation landlord`() {
        stubOrgLandlordWithCurrentUser()

        mvc
            .get(TEAM_MEMBERS_ROUTE)
            .andExpect {
                status { isOk() }
                view { name("teamMembers") }
                model { attributeExists("teamMembers") }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getTeamMembers adds the invite team member url to the model`() {
        stubOrgLandlordWithCurrentUser()

        mvc
            .get(TEAM_MEMBERS_ROUTE)
            .andExpect {
                model { attribute("inviteTeamMemberUrl", INVITE_TEAM_MEMBER_START_PATH) }
            }
    }
}
