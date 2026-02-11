package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DELETE_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.EDIT_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MANAGE_LOCAL_COUNCIL_ADMINS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.INVITE_LOCAL_COUNCIL_ADMIN_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.INVITE_LOCAL_COUNCIL_ADMIN_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.MANAGE_LOCAL_COUNCIL_ADMINS_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.SYSTEM_OPERATOR_ROUTE
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalCouncilUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilAdminInvitationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncil
import java.net.URI
import java.net.URLEncoder
import kotlin.test.Test

@WebMvcTest(ManageLocalCouncilAdminsController::class)
class ManageLocalCouncilAdminsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var localCouncilService: LocalCouncilService

    @MockitoBean
    lateinit var localCouncilDataService: LocalCouncilDataService

    @MockitoBean
    lateinit var emailNotificationService: EmailNotificationService<EmailTemplateModel>

    @MockitoBean
    lateinit var localCouncilInvitationService: LocalCouncilInvitationService

    @MockitoBean
    lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @MockitoBean
    lateinit var securityContextService: SecurityContextService

    @Test
    fun `inviteLocalCouncilAdmin redirects unauthenticated users`() {
        mvc.get(INVITE_LOCAL_COUNCIL_ADMIN_ROUTE).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `inviteLocalCouncilAdmin returns 403 for unauthorized users`() {
        mvc.get(INVITE_LOCAL_COUNCIL_ADMIN_ROUTE).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `inviteLocalCouncilAdmin returns 200 for authorized users`() {
        val localCouncils = listOf(createLocalCouncil())
        whenever(localCouncilService.retrieveAllLocalCouncils()).thenReturn(
            localCouncils,
        )

        mvc.get(INVITE_LOCAL_COUNCIL_ADMIN_ROUTE).andExpect {
            status { isOk() }
            model { attributeExists("selectOptions", "inviteLocalCouncilAdminModel") }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendInvitation sends an invitation email then redirects to success page for valid form submission`() {
        val localCouncil = createLocalCouncil(MockLocalCouncilData.DEFAULT_LOCAL_COUNCIL_ID)
        val testEmail = "new-user@example.com"
        val encodedTestEmail = URLEncoder.encode(testEmail, "UTF-8")
        val urlEncodedModel = "email=$encodedTestEmail&confirmEmail=$encodedTestEmail&localCouncilId=${localCouncil.id}"
        val invitationUri = URI("https://test-service.gov.uk/sign-up-local-council-user")

        whenever(localCouncilInvitationService.createInvitationToken(any(), any(), any()))
            .thenReturn("test-token")
        whenever(absoluteUrlProvider.buildInvitationUri("test-token"))
            .thenReturn(invitationUri)
        whenever(localCouncilService.retrieveLocalCouncilById(MockLocalCouncilData.DEFAULT_LOCAL_COUNCIL_ID))
            .thenReturn(localCouncil)

        mvc
            .post(INVITE_LOCAL_COUNCIL_ADMIN_ROUTE) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = urlEncodedModel
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl("${INVITE_LOCAL_COUNCIL_ADMIN_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
                flash { attribute("invitedEmailAddress", testEmail) }
                flash { attribute("localCouncilName", localCouncil.name) }
            }

        verify(emailNotificationService).sendEmail(testEmail, LocalCouncilAdminInvitationEmail(localCouncil, invitationUri))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `navigating straight to the confirmation page returns 400`() {
        mvc.get(INVITE_LOCAL_COUNCIL_ADMIN_CONFIRMATION_ROUTE).andExpect {
            status { isBadRequest() }
        }
    }

    @Nested
    inner class ManageAdmins {
        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `manageAdmins returns 403 for Local Council users`() {
            mvc
                .get(MANAGE_LOCAL_COUNCIL_ADMINS_ROUTE)
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `manageAdmins returns 403 for Local Council admin users`() {
            mvc
                .get(MANAGE_LOCAL_COUNCIL_ADMINS_ROUTE)
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `manageAdmins returns 200 for system operators`() {
            whenever(localCouncilDataService.getPaginatedAdminUsersAndInvitations(eq(0), anyOrNull()))
                .thenReturn(PageImpl(listOf(), PageRequest.of(0, 10), 1))
            mvc
                .get(MANAGE_LOCAL_COUNCIL_ADMINS_ROUTE)
                .andExpect {
                    status { isOk() }
                    model {
                        attributeExists("userList", "paginationViewModel")
                        attribute("cancelInvitationPathSegment", CANCEL_INVITATION_PATH_SEGMENT)
                        attribute("editUserPathSegment", EDIT_ADMIN_PATH_SEGMENT)
                        attribute("inviteAdminsUrl", INVITE_LOCAL_COUNCIL_ADMIN_ROUTE)
                    }
                }
        }
    }

    @Nested
    inner class EditAdmin {
        private val localCouncilAdmin = MockLocalCouncilData.createLocalCouncilUser(isManager = true)

        private val editLocalCouncilAdminRoute = "${SYSTEM_OPERATOR_ROUTE}/$EDIT_ADMIN_PATH_SEGMENT/${localCouncilAdmin.id}"

        @BeforeEach
        fun setup() {
            whenever(localCouncilDataService.getLocalCouncilUserById(localCouncilAdmin.id))
                .thenReturn(localCouncilAdmin)
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `editAdminsAccessLevel request returns 403 for Local Council users`() {
            mvc
                .get(editLocalCouncilAdminRoute)
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `editAdminsAccessLevel request returns 403 for Local Council admin users`() {
            mvc
                .get(editLocalCouncilAdminRoute)
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `editAdminsAccessLevel GET request returns 200 for system operators`() {
            mvc
                .get(editLocalCouncilAdminRoute)
                .andExpect {
                    status { isOk() }
                    model {
                        attributeExists("options")
                        attribute("backUrl", "../$MANAGE_LOCAL_COUNCIL_ADMINS_PATH_SEGMENT")
                        attribute("localCouncilUser", localCouncilAdmin)
                        attribute("deleteUserUrl", "$SYSTEM_OPERATOR_ROUTE/$DELETE_ADMIN_PATH_SEGMENT/${localCouncilAdmin.id}")
                    }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `editAdminsAccessLevel POST request returns redirect after editing admin for system operators`() {
            mvc
                .post(editLocalCouncilAdminRoute) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "isManager=false"
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl(MANAGE_LOCAL_COUNCIL_ADMINS_ROUTE)
                    }
                }

            verify(localCouncilDataService).updateUserAccessLevel(
                LocalCouncilUserAccessLevelRequestModel(false),
                localCouncilAdmin.id,
            )
        }
    }

    @Nested
    inner class DeleteAdmin {
        private val localCouncilAdmin = MockLocalCouncilData.createLocalCouncilUser(isManager = true)

        private val deleteLocalCouncilAdminRoute = "${SYSTEM_OPERATOR_ROUTE}/$DELETE_ADMIN_PATH_SEGMENT/${localCouncilAdmin.id}"

        @BeforeEach
        fun setup() {
            whenever(localCouncilDataService.getLocalCouncilUserById(localCouncilAdmin.id))
                .thenReturn(localCouncilAdmin)
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `deleteAdmin request returns 403 for Local Council users`() {
            mvc
                .get(deleteLocalCouncilAdminRoute)
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `deleteAdmin request returns 403 for Local Council admin users`() {
            mvc
                .get(deleteLocalCouncilAdminRoute)
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `deleteAdmin GET request returns 200 for system operators`() {
            mvc
                .get(deleteLocalCouncilAdminRoute)
                .andExpect {
                    status { isOk() }
                    model {
                        attribute("user", localCouncilAdmin)
                        attribute("backLinkPath", "../$EDIT_ADMIN_PATH_SEGMENT/${localCouncilAdmin.id}")
                    }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `deleteAdmin POST request returns redirect after deleting admin for system operators`() {
            mvc
                .post(deleteLocalCouncilAdminRoute) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl("../$DELETE_ADMIN_PATH_SEGMENT/${localCouncilAdmin.id}/$CONFIRMATION_PATH_SEGMENT")
                    }
                }

            verify(localCouncilDataService).deleteUser(localCouncilAdmin)
            verify(localCouncilDataService).addDeletedUserToSession(localCouncilAdmin)
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `deleteAdminConfirmation returns 404 if the user was not deleted in this session`() {
            whenever(localCouncilDataService.getUserDeletedThisSessionById(localCouncilAdmin.id))
                .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))
            mvc
                .get("$deleteLocalCouncilAdminRoute/$CONFIRMATION_PATH_SEGMENT")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `deleteAdminConfirmation returns 500 if the user is still in the database`() {
            whenever(localCouncilDataService.getUserDeletedThisSessionById(localCouncilAdmin.id))
                .thenThrow(ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))

            mvc
                .get("$deleteLocalCouncilAdminRoute/$CONFIRMATION_PATH_SEGMENT")
                .andExpect {
                    status { is5xxServerError() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `deleteAdminConfirmation returns 200 when user has been deleted in this session`() {
            whenever(localCouncilDataService.getUserDeletedThisSessionById(localCouncilAdmin.id)).thenReturn(localCouncilAdmin)

            mvc
                .get("$deleteLocalCouncilAdminRoute/$CONFIRMATION_PATH_SEGMENT")
                .andExpect {
                    status { isOk() }
                    model {
                        attribute("deletedUserName", localCouncilAdmin.name)
                        attribute("localCouncil", localCouncilAdmin.localCouncil)
                        attribute("returnToManageUsersUrl", MANAGE_LOCAL_COUNCIL_ADMINS_ROUTE)
                    }
                }
        }
    }

    @Nested
    inner class CancelAdminInvitation {
        private val localCouncilAdminInvite = MockLocalCouncilData.createLocalCouncilInvitation(invitedAsAdmin = true)

        private val cancelLocalCouncilAdminInviteRoute =
            "${SYSTEM_OPERATOR_ROUTE}/$CANCEL_INVITATION_PATH_SEGMENT/${localCouncilAdminInvite.id}"

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `cancelAdminInvitation request returns 403 for Local Council users`() {
            mvc
                .get(cancelLocalCouncilAdminInviteRoute)
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `cancelAdminInvitation request returns 403 for Local Council admin users`() {
            mvc
                .get(cancelLocalCouncilAdminInviteRoute)
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `cancelAdminInvitation GET request returns 404 when admin invitation is not found`() {
            whenever(localCouncilInvitationService.getAdminInvitationByIdOrNull(localCouncilAdminInvite.id))
                .thenReturn(null)

            mvc
                .get(cancelLocalCouncilAdminInviteRoute)
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `cancelAdminInvitation GET request returns 200 for system operators when admin invitation exists`() {
            whenever(localCouncilInvitationService.getAdminInvitationByIdOrNull(localCouncilAdminInvite.id))
                .thenReturn(localCouncilAdminInvite)

            mvc
                .get(cancelLocalCouncilAdminInviteRoute)
                .andExpect {
                    status { isOk() }
                    model {
                        attribute("backLinkPath", "../$MANAGE_LOCAL_COUNCIL_ADMINS_PATH_SEGMENT")
                        attribute("email", localCouncilAdminInvite.invitedEmail)
                    }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `cancelAdminInvitation POST request returns 404 when admin invitation is not found`() {
            whenever(localCouncilInvitationService.getAdminInvitationByIdOrNull(localCouncilAdminInvite.id))
                .thenReturn(null)

            mvc
                .post(cancelLocalCouncilAdminInviteRoute) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `cancelAdminInvitation POST request returns redirect after deleting admin invite for system operators`() {
            whenever(localCouncilInvitationService.getAdminInvitationByIdOrNull(localCouncilAdminInvite.id))
                .thenReturn(localCouncilAdminInvite)

            mvc
                .post(cancelLocalCouncilAdminInviteRoute) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl("../$CANCEL_INVITATION_PATH_SEGMENT/${localCouncilAdminInvite.id}/$CONFIRMATION_PATH_SEGMENT")
                    }
                }

            verify(localCouncilInvitationService).deleteInvitation(localCouncilAdminInvite.id)
            verify(localCouncilDataService).addCancelledInvitationToSession(localCouncilAdminInvite)
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `cancelAdminInvitationConfirmation returns 404 if the invite was not deleted in the session`() {
            whenever(localCouncilDataService.getInvitationCancelledThisSessionById(localCouncilAdminInvite.id))
                .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

            mvc
                .get("$cancelLocalCouncilAdminInviteRoute/$CONFIRMATION_PATH_SEGMENT")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `cancelAdminInvitationConfirmation returns 500 if the invitation is still in the database`() {
            whenever(localCouncilDataService.getInvitationCancelledThisSessionById(localCouncilAdminInvite.id))
                .thenThrow(ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))

            mvc
                .get("$cancelLocalCouncilAdminInviteRoute/$CONFIRMATION_PATH_SEGMENT")
                .andExpect {
                    status { isInternalServerError() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `cancelAdminInvitationConfirmation returns 200 when invite has been deleted in this session`() {
            whenever(
                localCouncilDataService.getInvitationCancelledThisSessionById(localCouncilAdminInvite.id),
            ).thenReturn(localCouncilAdminInvite)

            mvc
                .get("$cancelLocalCouncilAdminInviteRoute/$CONFIRMATION_PATH_SEGMENT")
                .andExpect {
                    status { isOk() }
                    model {
                        attribute("deletedEmail", localCouncilAdminInvite.invitedEmail)
                        attribute("localCouncil", localCouncilAdminInvite.invitingCouncil)
                        attribute("returnToManageUsersUrl", MANAGE_LOCAL_COUNCIL_ADMINS_ROUTE)
                    }
                }
        }
    }
}
