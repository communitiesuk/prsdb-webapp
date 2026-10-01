package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.MessageSourceConfig
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord

@WebMvcTest(TeamMembersController::class)
@Import(MessageSourceConfig::class)
class TeamMembersControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    private lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    private fun stubOrgLandlordWithCurrentUserRole(role: OrganisationalLandlordUserRole) {
        val organisation = createOrgLandlord()
        val currentUser = OrganisationalLandlordUser(organisation, PrsdbUser("user-123"), "Current User", "user@example.com", role)
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
    @WithMockUser(roles = ["LANDLORD"], username = "user-123")
    fun `getTeamMembers returns 403 for a non-organisation landlord`() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())

        mvc
            .get(TEAM_MEMBERS_ROUTE)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user-123")
    fun `getTeamMembers shows the invite button to an organisation admin`() {
        stubOrgLandlordWithCurrentUserRole(OrganisationalLandlordUserRole.ADMIN)

        mvc
            .get(TEAM_MEMBERS_ROUTE)
            .andExpect {
                status { isOk() }
                view { name("teamMembers") }
                model {
                    attributeExists("teamMembers")
                    attribute("inviteTeamMemberUrl", "#")
                }
                content { string(containsString("Invite a team member")) }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user-123")
    fun `getTeamMembers hides the invite button from an organisation editor`() {
        stubOrgLandlordWithCurrentUserRole(OrganisationalLandlordUserRole.EDITOR)

        mvc
            .get(TEAM_MEMBERS_ROUTE)
            .andExpect {
                status { isOk() }
                model {
                    attributeExists("teamMembers")
                    attributeDoesNotExist("inviteTeamMemberUrl")
                }
                content { string(not(containsString("Invite a team member"))) }
            }
    }
}
