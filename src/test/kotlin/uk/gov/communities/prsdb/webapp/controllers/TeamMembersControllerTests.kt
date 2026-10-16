package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole.ADMIN
import uk.gov.communities.prsdb.webapp.controllers.InviteOrganisationalLandlordUserController.Companion.INVITE_ORGANISATIONAL_LANDLORD_USER_PATH
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.INVITATIONS_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.TeamMembersViewModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrganisationalLandlordInvitation
import kotlin.test.assertEquals

@WebMvcTest(TeamMembersController::class)
class TeamMembersControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    private lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    @MockitoBean
    private lateinit var organisationalLandlordInvitationService: OrganisationalLandlordInvitationService

    private val resendRoute = TeamMembersController.getResendInvitationPath(123L)

    private fun stubOrgLandlordWithCurrentUser() {
        val organisation = createOrgLandlord()
        val currentUser =
            OrganisationalLandlordUser(organisation, PrsdbUser("user-123"), "Current User", "user@example.com", ADMIN)
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(organisation)
        whenever(organisationalLandlordUserService.getOrganisationalLandlordUsers(organisation)).thenReturn(
            listOf(
                currentUser,
            ),
        )
        whenever(organisationalLandlordInvitationService.getInvitationsForOrganisation(organisation))
            .thenReturn(listOf(createOrganisationalLandlordInvitation(organisationalLandlord = organisation)))
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
                model { attribute("inviteTeamMemberUrl", INVITE_ORGANISATIONAL_LANDLORD_USER_PATH) }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getTeamMembers adds the organisation's invitations and the invitations tab id to the model`() {
        stubOrgLandlordWithCurrentUser()

        val result =
            mvc
                .get(TEAM_MEMBERS_ROUTE)
                .andExpect {
                    status { isOk() }
                    model {
                        attribute("invitationsTabId", INVITATIONS_FRAGMENT)
                        attribute("invitationLifetimeInDays", ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS)
                    }
                }.andReturn()

        val teamMembers = result.modelAndView!!.model["teamMembers"] as TeamMembersViewModel
        assertEquals(1, teamMembers.pendingInvitationRows.size)
    }

    @Test
    fun `resendInvitation returns a redirect for unauthenticated user`() {
        mvc
            .get(resendRoute)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `resendInvitation returns 403 for an unauthorised user`() {
        mvc
            .get(resendRoute)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `resendInvitation returns 403 for a non-organisation landlord`() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())

        mvc
            .get(resendRoute)
            .andExpect {
                status { isForbidden() }
            }
        verify(organisationalLandlordInvitationService, never()).resendInvitation(any(), any())
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `resendInvitation redirects to the invitations tab with the resent email as a flash attribute`() {
        val organisation = createOrgLandlord()
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(organisation)
        whenever(organisationalLandlordInvitationService.resendInvitation(eq(123L), eq(organisation)))
            .thenReturn("invitee@example.com")

        mvc
            .get(resendRoute)
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl("$TEAM_MEMBERS_ROUTE#$INVITATIONS_FRAGMENT")
                flash { attribute("resendInvitationEmail", "invitee@example.com") }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `resendInvitation redirects to the invitations tab without a flash attribute when the invitation was not resent`() {
        val organisation = createOrgLandlord()
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(organisation)
        whenever(organisationalLandlordInvitationService.resendInvitation(eq(123L), eq(organisation))).thenReturn(null)

        mvc
            .get(resendRoute)
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl("$TEAM_MEMBERS_ROUTE#$INVITATIONS_FRAGMENT")
                flash { attributeCount(0) }
            }
    }
}
