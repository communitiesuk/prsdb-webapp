package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.MessageSourceConfig
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
@Import(MessageSourceConfig::class)
class TeamMembersControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    private lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    @MockitoBean
    private lateinit var organisationalLandlordInvitationService: OrganisationalLandlordInvitationService

    private val removeExpiredInvitationRoute = TeamMembersController.getRemoveExpiredInvitationPath(1)

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
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getTeamMembers renders the access removed banner when flash attributes are present`() {
        stubOrgLandlordWithCurrentUser()

        mvc
            .get(TEAM_MEMBERS_ROUTE) {
                flashAttr("successBanner", "accessRemoved")
                flashAttr("accessRemovedMemberName", "Anita Locke")
            }.andExpect {
                status { isOk() }
                content { string(containsString("Access removed")) }
                content { string(containsString("Anita Locke no longer has access to the service.")) }
            }
    }

    @ParameterizedTest
    @CsvSource(
        "teamMembers.accessLevelChangedBanner.content.administrator, administrator",
        "teamMembers.accessLevelChangedBanner.content.editor, editor",
    )
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getTeamMembers renders the access level changed banner for the selected role`(
        bannerContentKey: String,
        roleDescription: String,
    ) {
        stubOrgLandlordWithCurrentUser()

        mvc
            .get(TEAM_MEMBERS_ROUTE) {
                flashAttr("successBanner", "accessLevelChanged")
                flashAttr("accessLevelChangedMemberName", "Anita Locke")
                flashAttr("accessLevelChangedBannerContentKey", bannerContentKey)
            }.andExpect {
                status { isOk() }
                content { string(containsString("Access level changed")) }
                content { string(containsString("Anita Locke is now an $roleDescription.")) }
                content { string(not(containsString("Invitation sent"))) }
                content { string(not(containsString("Access removed"))) }
                content { string(not(containsString("Invitation resent"))) }
            }
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
    @WithMockUser(roles = ["ORG_EDITOR"])
    fun `resendInvitation returns 403 for an organisation editor`() {
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
                flash { attribute("successBanner", "resendInvitation") }
                flash { attribute("resendInvitationEmail", "invitee@example.com") }
            }
    }

    @Test
    fun `removeExpiredInvitation returns a redirect for unauthenticated user`() {
        mvc
            .get(removeExpiredInvitationRoute)
            .andExpect {
                status { is3xxRedirection() }
            }

        verify(organisationalLandlordInvitationService, never()).hideExpiredInvitation(any(), any())
    }

    @Test
    @WithMockUser
    fun `removeExpiredInvitation returns 403 for an unauthorised user`() {
        mvc
            .get(removeExpiredInvitationRoute)
            .andExpect {
                status { isForbidden() }
            }
    }

    @ParameterizedTest
    @ValueSource(strings = ["INDIVIDUAL_LANDLORD", "ORG_EDITOR", "LOCAL_COUNCIL_ADMIN", "LOCAL_COUNCIL_USER", "SYSTEM_OPERATOR"])
    fun `removeExpiredInvitation returns 403 for roles other than organisation admin`(role: String) {
        mvc
            .get(removeExpiredInvitationRoute) {
                with(user("user").roles(role))
            }.andExpect {
                status { isForbidden() }
            }

        verify(organisationalLandlordInvitationService, never()).hideExpiredInvitation(any(), any())
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `removeExpiredInvitation returns 403 for a non-organisation landlord`() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())

        mvc
            .get(removeExpiredInvitationRoute)
            .andExpect {
                status { isForbidden() }
            }

        verify(organisationalLandlordInvitationService, never()).hideExpiredInvitation(any(), any())
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `removeExpiredInvitation hides the invitation and redirects to the invitations tab`() {
        val organisation = createOrgLandlord()
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(organisation)

        mvc
            .get(removeExpiredInvitationRoute)
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl("$TEAM_MEMBERS_ROUTE#$INVITATIONS_FRAGMENT")
            }

        verify(organisationalLandlordInvitationService).hideExpiredInvitation(1, organisation)
    }
}
