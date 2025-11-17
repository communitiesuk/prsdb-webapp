package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.EDIT_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.INVITE_USER_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getCancelInviteConfirmationRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getDeleteUserConfirmationRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getLocalCouncilCancelInviteRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getLocalCouncilCancelInviteSuccessRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getLocalCouncilDeleteUserRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getLocalCouncilDeleteUserSuccessRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getLocalCouncilEditUserRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getLocalCouncilInviteNewUserRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getLocalCouncilInviteUserSuccessRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getLocalCouncilManageUsersRoute
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilUser
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalCouncilUserDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalCouncilUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilInvitationCancellationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilInvitationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.DEFAULT_LOCAL_COUNCIL_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.DEFAULT_LOCAL_COUNCIL_INVITATION_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.DEFAULT_LOCAL_COUNCIL_USER_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.DEFAULT_LOGGED_IN_LOCAL_COUNCIL_USER_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.NON_ADMIN_LOCAL_COUNCIL_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncil
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncilUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createdLoggedInUserModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockOneLoginUserData.Companion.createOneLoginUser
import java.net.URI
import java.net.URLEncoder
import kotlin.test.Test

@WebMvcTest(ManageLocalCouncilUsersController::class)
class ManageLocalCouncilUsersControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var emailNotificationService: EmailNotificationService<EmailTemplateModel>

    @MockitoBean
    lateinit var localCouncilInvitationService: LocalCouncilInvitationService

    @MockitoBean
    lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @MockitoBean
    lateinit var localCouncilService: LocalCouncilService

    @MockitoBean
    private lateinit var localCouncilDataService: LocalCouncilDataService

    @MockitoBean
    private lateinit var securityContextService: SecurityContextService

    @Nested
    inner class ManageUsersPage {
        @Test
        fun `index returns a redirect for unauthenticated user`() {
            mvc.get(getLocalCouncilManageUsersRoute(DEFAULT_LOCAL_COUNCIL_ID)).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `index returns 403 for unauthorized user`() {
            mvc
                .get(getLocalCouncilManageUsersRoute(DEFAULT_LOCAL_COUNCIL_ID))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `index returns 403 for a local council (non-admin) user`() {
            mvc
                .get(getLocalCouncilManageUsersRoute(DEFAULT_LOCAL_COUNCIL_ID))
                .andExpect {
                    status { isForbidden() }
                }

            verify(localCouncilDataService, never()).getPaginatedUsersAndInvitations(any(), any(), anyOrNull(), anyOrNull())
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `index returns 200 for authorized user`() {
            val loggedInUserModel = createdLoggedInUserModel()
            val localCouncil = LocalCouncil(DEFAULT_LOCAL_COUNCIL_ID, "Test Local Council", "custodianCode")
            whenever(localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(DEFAULT_LOCAL_COUNCIL_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localCouncil))
            whenever(localCouncilDataService.getPaginatedUsersAndInvitations(localCouncil, 0))
                .thenReturn(PageImpl(listOf(), PageRequest.of(0, 10), 1))

            mvc
                .get(getLocalCouncilManageUsersRoute(DEFAULT_LOCAL_COUNCIL_ID))
                .andExpect {
                    status { isOk() }
                    model {
                        attribute("currentUserId", loggedInUserModel.id)
                        attribute("localCouncil", localCouncil)
                    }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `index returns 403 for admin user accessing another LA`() {
            whenever(localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(DEFAULT_LOCAL_COUNCIL_ID, "user"))
                .thenThrow(AccessDeniedException(""))

            mvc
                .get(getLocalCouncilManageUsersRoute(DEFAULT_LOCAL_COUNCIL_ID))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `index returns 200 for a system operator`() {
            val localCouncil = setupLocalCouncilForSystemOperator(NON_ADMIN_LOCAL_COUNCIL_ID)
            whenever(localCouncilDataService.getPaginatedUsersAndInvitations(eq(localCouncil), eq(0), anyOrNull(), anyOrNull()))
                .thenReturn(PageImpl(listOf(), PageRequest.of(0, 10), 1))
            mvc
                .get(getLocalCouncilManageUsersRoute(NON_ADMIN_LOCAL_COUNCIL_ID))
                .andExpect {
                    status { isOk() }
                    model {
                        attribute("localCouncil", localCouncil)
                    }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `index returns 404 for authorized user accessing a page less than 1`() {
            val loggedInUserModel = createdLoggedInUserModel()
            val localCouncil = LocalCouncil(DEFAULT_LOCAL_COUNCIL_ID, "Test Local Council", "custodianCode")
            whenever(localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(DEFAULT_LOCAL_COUNCIL_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localCouncil))
            whenever(localCouncilDataService.getPaginatedUsersAndInvitations(localCouncil, 0))
                .thenReturn(PageImpl(listOf(), PageRequest.of(0, 10), 1))

            mvc
                .get("${getLocalCouncilManageUsersRoute(DEFAULT_LOCAL_COUNCIL_ID)}?page=0")
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    @Nested
    inner class InviteNewUser {
        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `sendInvitation as an la admin creates an invitation token`() {
            val invitedEmail = "new-user@example.com"

            val localCouncil = setupSuccessfulPostToSendInvitationAndReturnLocalCouncil()

            postToSendInvitationAndAssertRedirectionToConfirmation(DEFAULT_LOCAL_COUNCIL_ID, invitedEmail)

            verify(localCouncilInvitationService).createInvitationToken(invitedEmail, localCouncil, false)
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `sendInvitation emails the invited user`() {
            val invitationUrl = "https://test-service.gov.uk/sign-up-la-user"
            val dashboardUrl = "https://test-service.gov.uk"
            val localCouncil =
                setupSuccessfulPostToSendInvitationAndReturnLocalCouncil(
                    invitationUrl = invitationUrl,
                    dashboardUrl = dashboardUrl,
                )

            val invitedEmail = "new-user@example.com"

            postToSendInvitationAndAssertRedirectionToConfirmation(DEFAULT_LOCAL_COUNCIL_ID, invitedEmail)

            verify(emailNotificationService)
                .sendEmail(
                    invitedEmail,
                    LocalCouncilInvitationEmail(
                        localCouncil,
                        URI(invitationUrl),
                        dashboardUrl,
                    ),
                )
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `sendInvitation emails admins when a new user is invited`() {
            val localCouncil = setupSuccessfulPostToSendInvitationAndReturnLocalCouncil()

            val invitedEmail = "new-user@example.com"

            postToSendInvitationAndAssertRedirectionToConfirmation(DEFAULT_LOCAL_COUNCIL_ID, invitedEmail)

            verify(localCouncilDataService)
                .sendUserInvitedEmailsToAdmins(localCouncil, invitedEmail)
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `sendInvitation adds the invited user to the session`() {
            val localCouncil = setupSuccessfulPostToSendInvitationAndReturnLocalCouncil()

            val invitedEmail = "new-user@example.com"

            postToSendInvitationAndAssertRedirectionToConfirmation(DEFAULT_LOCAL_COUNCIL_ID, invitedEmail)

            verify(localCouncilDataService).addInvitedLocalCouncilUserToSession(localCouncil.id, invitedEmail)
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `sendInvitation as an la admin with valid form redirects to confirmation page`() {
            val invitedEmail = "new-user@example.com"

            setupSuccessfulPostToSendInvitationAndReturnLocalCouncil()

            postToSendInvitationAndAssertRedirectionToConfirmation(DEFAULT_LOCAL_COUNCIL_ID, invitedEmail)
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `sendInvitation as a system operator with valid form creates the tokens, sends emails and redirects as for an LA admin`() {
            val invitationUrl = "https://test-service.gov.uk/sign-up-la-user"
            val dashboardUrl = "https://test-service.gov.uk"
            val localCouncil =
                setupSuccessfulPostToSendInvitationAndReturnLocalCouncil(
                    userIsSystemOperator = true,
                    invitationUrl = invitationUrl,
                    dashboardUrl = dashboardUrl,
                )

            val invitedEmail = "new-user@example.com"

            postToSendInvitationAndAssertRedirectionToConfirmation(NON_ADMIN_LOCAL_COUNCIL_ID, invitedEmail)

            verify(localCouncilInvitationService).createInvitationToken(invitedEmail, localCouncil, false)

            verify(emailNotificationService)
                .sendEmail(
                    invitedEmail,
                    LocalCouncilInvitationEmail(
                        localCouncil,
                        URI(invitationUrl),
                        dashboardUrl,
                    ),
                )
            verify(localCouncilDataService)
                .sendUserInvitedEmailsToAdmins(localCouncil, invitedEmail)
            verify(localCouncilDataService).addInvitedLocalCouncilUserToSession(localCouncil.id, invitedEmail)
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `inviteNewUserConfirmation returns 200 if a user was invited to the requested local council this session`() {
            setupDefaultLocalCouncilForLaAdmin()
            whenever((localCouncilDataService.getLastLocalCouncilUserInvitedThisSession(DEFAULT_LOCAL_COUNCIL_ID)))
                .thenReturn("invited.email@example.com")

            mvc.get(getLocalCouncilInviteUserSuccessRoute(DEFAULT_LOCAL_COUNCIL_ID)).andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `inviteNewUserConfirmation returns 404 if no user was invited to the requested local council this session`() {
            whenever((localCouncilDataService.getLastUserIdRegisteredThisSession())).thenReturn(null)

            mvc.get(getLocalCouncilInviteUserSuccessRoute(DEFAULT_LOCAL_COUNCIL_ID)).andExpect {
                status { isNotFound() }
            }
        }

        private fun setupSuccessfulPostToSendInvitationAndReturnLocalCouncil(
            userIsSystemOperator: Boolean = false,
            invitationUrl: String = "https://test-service.gov.uk/sign-up-la-user",
            dashboardUrl: String = "https://test-service.gov.uk",
        ): LocalCouncil {
            whenever(localCouncilInvitationService.createInvitationToken(any(), any(), any()))
                .thenReturn("test-token")
            whenever(absoluteUrlProvider.buildInvitationUri("test-token"))
                .thenReturn(URI(invitationUrl))
            whenever(absoluteUrlProvider.buildLocalCouncilDashboardUri()).thenReturn(URI(dashboardUrl))

            return if (userIsSystemOperator) {
                setupLocalCouncilForSystemOperator(NON_ADMIN_LOCAL_COUNCIL_ID)
            } else {
                setupDefaultLocalCouncilForLaAdmin()
            }
        }

        private fun postToSendInvitationAndAssertRedirectionToConfirmation(
            laId: Int = DEFAULT_LOCAL_COUNCIL_ID,
            invitedEmail: String,
        ) {
            mvc
                .post(getLocalCouncilInviteNewUserRoute(laId)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = urlEncodedConfirmedEmailDataModel(invitedEmail)
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(INVITE_USER_CONFIRMATION_ROUTE)
                }
        }

        private fun urlEncodedConfirmedEmailDataModel(
            @Suppress("SameParameterValue") testEmail: String,
        ): String {
            val encodedTestEmail = URLEncoder.encode(testEmail, "UTF-8")
            return "email=$encodedTestEmail&confirmEmail=$encodedTestEmail"
        }
    }

    @Nested
    inner class EditUser {
        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `getEditUserAccessLevelPage returns 403 for admin user accessing another LA`() {
            createdLoggedInUserModel()
            whenever(localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(DEFAULT_LOCAL_COUNCIL_ID, "user"))
                .thenThrow(AccessDeniedException(""))

            mvc
                .get(getLocalCouncilEditUserRoute(DEFAULT_LOCAL_COUNCIL_ID, 1))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `getEditUserAccessLevelPage returns 404 for admin user accessing a LA user that does not exist or is from another LA`() {
            setupDefaultLocalCouncilForLaAdmin()
            whenever(
                localCouncilDataService.getLocalCouncilUserIfAuthorizedLocalCouncil(
                    DEFAULT_LOCAL_COUNCIL_USER_ID,
                    DEFAULT_LOCAL_COUNCIL_ID,
                ),
            ).thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

            mvc
                .get(getLocalCouncilEditUserRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_USER_ID))
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `getEditUserAccessLevelPage returns 404 for admin user specifying a non-number for the user id`() {
            mvc
                .get("/$LOCAL_COUNCIL_PATH_SEGMENT/$DEFAULT_LOCAL_COUNCIL_ID/$EDIT_USER_PATH_SEGMENT/not-a-number")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `getEditUserAccessLevelPage returns 403 for admin user accessing their own edit page`() {
            val loggedInUser = createLocalCouncilUser(name = "Logged In User")
            val loggedInUserModel = LocalCouncilUserDataModel.fromLocalCouncilUser(loggedInUser)
            val localCouncil = createLocalCouncil()
            whenever(localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(DEFAULT_LOCAL_COUNCIL_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localCouncil))
            whenever(localCouncilDataService.getLocalCouncilUserIfAuthorizedLocalCouncil(loggedInUserModel.id, DEFAULT_LOCAL_COUNCIL_ID))
                .thenReturn(loggedInUser)

            mvc
                .get(getLocalCouncilEditUserRoute(DEFAULT_LOCAL_COUNCIL_ID, loggedInUserModel.id))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `getEditUserAccessLevelPage returns 200 for admin user accessing a user from its LA`() {
            val localCouncil = setupDefaultLocalCouncilForLaAdmin()

            setupLocalCouncilUserToEdit(localCouncil)

            mvc
                .get(getLocalCouncilEditUserRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_USER_ID))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("localCouncilUser", "options") }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `getEditUserAccessLevelPage returns 200 for system operator`() {
            val localCouncil = setupLocalCouncilForSystemOperator(NON_ADMIN_LOCAL_COUNCIL_ID)

            setupLocalCouncilUserToEdit(localCouncil)

            mvc
                .get(getLocalCouncilEditUserRoute(NON_ADMIN_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_USER_ID))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("localCouncilUser", "options") }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `updateUserAccessLevel gives a 403 when trying to update currently logged in user`() {
            val loggedInUserModel = createdLoggedInUserModel()
            val localCouncil = createLocalCouncil()
            whenever(localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(DEFAULT_LOCAL_COUNCIL_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localCouncil))

            mvc
                .post(getLocalCouncilEditUserRoute(DEFAULT_LOCAL_COUNCIL_ID, loggedInUserModel.id)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "isManager=false"
                    with(csrf())
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `updateUserAccessLevel updates the given user's access level when called by an la admin`() {
            setupDefaultLocalCouncilForLaAdmin()

            postUpdateUserAccessLevelAndAssertSuccess(DEFAULT_LOCAL_COUNCIL_ID)
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `updateUserAccessLevel updates the given user's access level when called by a system operator`() {
            setupLocalCouncilForSystemOperator(NON_ADMIN_LOCAL_COUNCIL_ID)

            postUpdateUserAccessLevelAndAssertSuccess()
        }

        private fun postUpdateUserAccessLevelAndAssertSuccess(laId: Int = DEFAULT_LOCAL_COUNCIL_ID) {
            mvc
                .post(getLocalCouncilEditUserRoute(laId, DEFAULT_LOCAL_COUNCIL_USER_ID)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "isManager=true"
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl(getLocalCouncilManageUsersRoute(DEFAULT_LOCAL_COUNCIL_ID))
                    }
                }

            verify(localCouncilDataService).updateUserAccessLevel(
                LocalCouncilUserAccessLevelRequestModel(true),
                DEFAULT_LOCAL_COUNCIL_USER_ID,
            )
        }
    }

    @Nested
    inner class DeleteUser {
        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `confirmDeleteUser gives a 200 for admins of the LA containing the user`() {
            val localCouncil = setupDefaultLocalCouncilForLaAdmin()
            setupLocalCouncilUserToEdit(localCouncil)

            mvc
                .get(getLocalCouncilDeleteUserRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_USER_ID))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("user") }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `confirmDeleteUser gives a 200 for system operators`() {
            val localCouncil = setupLocalCouncilForSystemOperator(NON_ADMIN_LOCAL_COUNCIL_ID)
            setupLocalCouncilUserToEdit(localCouncil)

            mvc
                .get(getLocalCouncilDeleteUserRoute(NON_ADMIN_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_USER_ID))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("user") }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `confirmDeleteUser gives a 403 when trying to delete currently logged in user`() {
            val loggedInUserModel = createdLoggedInUserModel()
            val localCouncil = createLocalCouncil()
            whenever(localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(DEFAULT_LOCAL_COUNCIL_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localCouncil))

            mvc
                .post(getLocalCouncilDeleteUserRoute(DEFAULT_LOCAL_COUNCIL_ID, loggedInUserModel.id)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "isManager=false"
                    with(csrf())
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `deleteUser deletes the specified user for an la admin and adds their details to the session`() {
            val localCouncil = setupDefaultLocalCouncilForLaAdmin()

            val user = setupLocalCouncilUserToEdit(localCouncil)

            postDeleteUserAndAssertSuccess(DEFAULT_LOCAL_COUNCIL_ID, user)

            verify(localCouncilDataService).addDeletedUserToSession(user)
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `deleteUser deletes the specified user for a system operator and adds their details to the session`() {
            val localCouncil = setupLocalCouncilForSystemOperator(NON_ADMIN_LOCAL_COUNCIL_ID)

            val user = setupLocalCouncilUserToEdit(localCouncil)

            postDeleteUserAndAssertSuccess(NON_ADMIN_LOCAL_COUNCIL_ID, user)

            verify(localCouncilDataService).addDeletedUserToSession(user)
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `deleteUser returns 403 if a user who is not a system operator attempts to delete themself`() {
            val loggedInUserModel = createdLoggedInUserModel()
            val localCouncil = createLocalCouncil()
            whenever(localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(DEFAULT_LOCAL_COUNCIL_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localCouncil))

            mvc
                .post(getLocalCouncilDeleteUserRoute(DEFAULT_LOCAL_COUNCIL_ID, loggedInUserModel.id)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR", "LOCAL_COUNCIL_ADMIN"])
        fun `deleteUser allows a system operator to delete themself then refreshes the user roles`() {
            val localCouncil = setupLocalCouncilForSystemOperator(DEFAULT_LOCAL_COUNCIL_ID)
            val user = setupLocalCouncilUserToEdit(localCouncil, DEFAULT_LOGGED_IN_LOCAL_COUNCIL_USER_ID)
            whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(user)

            postDeleteUserAndAssertSuccess(DEFAULT_LOCAL_COUNCIL_ID, user)

            verify(securityContextService).refreshContext()
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR", "LOCAL_COUNCIL_ADMIN"])
        fun `deleteUser does not refresh user roles if the logged in user was not deleted`() {
            val localCouncil = setupLocalCouncilForSystemOperator(DEFAULT_LOCAL_COUNCIL_ID)
            val user = setupLocalCouncilUserToEdit(localCouncil, DEFAULT_LOCAL_COUNCIL_USER_ID)
            val loggedInUser = createLocalCouncilUser(id = DEFAULT_LOGGED_IN_LOCAL_COUNCIL_USER_ID)
            whenever(localCouncilDataService.getLocalCouncilUser("user")).thenReturn(loggedInUser)

            postDeleteUserAndAssertSuccess(DEFAULT_LOCAL_COUNCIL_ID, user)

            verify(securityContextService, never()).refreshContext()
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `deleteUserSuccess returns 200 if the user was deleted this session`() {
            setupDefaultLocalCouncilForLaAdmin()
            val localCouncilUser = createLocalCouncilUser(id = DEFAULT_LOCAL_COUNCIL_USER_ID)
            whenever(localCouncilDataService.getUserDeletedThisSessionById(localCouncilUser.id))
                .thenReturn(localCouncilUser)
            mvc
                .get(getLocalCouncilDeleteUserSuccessRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_USER_ID))
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `deleteUserSuccess returns 404 if the user was not deleted in this session`() {
            whenever(localCouncilDataService.getUserDeletedThisSessionById(DEFAULT_LOCAL_COUNCIL_USER_ID))
                .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))
            mvc
                .get(getLocalCouncilDeleteUserSuccessRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_USER_ID))
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `delete user success page returns 500 if the user is still in the database`() {
            whenever(localCouncilDataService.getUserDeletedThisSessionById(DEFAULT_LOCAL_COUNCIL_USER_ID))
                .thenThrow(ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))

            mvc
                .get(getLocalCouncilDeleteUserSuccessRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_USER_ID))
                .andExpect {
                    status { is5xxServerError() }
                }
        }

        private fun postDeleteUserAndAssertSuccess(
            laId: Int = DEFAULT_LOCAL_COUNCIL_ID,
            userBeingDeleted: LocalCouncilUser = createLocalCouncilUser(id = DEFAULT_LOCAL_COUNCIL_USER_ID),
        ): LocalCouncilUser {
            whenever(localCouncilDataService.getLocalCouncilUserIfAuthorizedLocalCouncil(userBeingDeleted.id, laId))
                .thenReturn(userBeingDeleted)

            mvc
                .post(getLocalCouncilDeleteUserRoute(laId, userBeingDeleted.id)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl("../${getDeleteUserConfirmationRoute(userBeingDeleted.id)}")
                    }
                }

            verify(localCouncilDataService).deleteUser(userBeingDeleted)

            return userBeingDeleted
        }
    }

    @Nested
    inner class CancelInvitation {
        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `confirmCancelInvitation returns a 200 for admins of the inviting LA`() {
            setupDefaultLocalCouncilForLaAdmin()

            val invitation = createLocalCouncilInvitation()
            whenever(localCouncilInvitationService.getInvitationByIdOrNull(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)).thenReturn(invitation)

            mvc
                .get(getLocalCouncilCancelInviteRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_INVITATION_ID))
                .andExpect {
                    status { isOk() }
                    model { attribute("email", invitation.invitedEmail) }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `confirmCancelInvitation returns a 200 for system operator`() {
            setupLocalCouncilForSystemOperator(NON_ADMIN_LOCAL_COUNCIL_ID)

            val invitation = createLocalCouncilInvitation(invitingCouncil = createLocalCouncil(id = NON_ADMIN_LOCAL_COUNCIL_ID))
            whenever(localCouncilInvitationService.getInvitationByIdOrNull(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)).thenReturn(invitation)

            mvc
                .get(getLocalCouncilCancelInviteRoute(NON_ADMIN_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_INVITATION_ID))
                .andExpect {
                    status { isOk() }
                    model { attribute("email", invitation.invitedEmail) }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `confirmCancelInvitation returns 403 for admin user accessing another LA`() {
            whenever(localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(DEFAULT_LOCAL_COUNCIL_ID, "user"))
                .thenThrow(AccessDeniedException(""))

            val invitation = createLocalCouncilInvitation()
            whenever(localCouncilInvitationService.getInvitationByIdOrNull(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)).thenReturn(invitation)

            mvc
                .get(getLocalCouncilCancelInviteRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_INVITATION_ID))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `confirmCancelInvitation returns 403 for admin user accessing an invitation from another LA`() {
            setupDefaultLocalCouncilForLaAdmin()

            val invitation = createLocalCouncilInvitation(invitingCouncil = createLocalCouncil(id = 789))
            whenever(localCouncilInvitationService.getInvitationByIdOrNull(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)).thenReturn(invitation)

            mvc
                .get(getLocalCouncilCancelInviteRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_INVITATION_ID))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `cancelInvitation returns 404 if the invitation is not in the database`() {
            whenever(localCouncilInvitationService.getInvitationByIdOrNull(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)).thenReturn(null)

            mvc
                .post(getLocalCouncilCancelInviteRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_INVITATION_ID)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `cancelInvitation removes the invitation from the database and adds it to the session when called by an la admin`() {
            setupInvitationPostToCancelInvitationAndAssertSuccess()
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `cancelInvitation removes the invitation from the database and adds it to the session when called by a system operator`() {
            setupInvitationPostToCancelInvitationAndAssertSuccess()
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `cancelInvitation emails a cancellation notification to the invited email address`() {
            val invitation = createLocalCouncilInvitation()
            whenever(localCouncilInvitationService.getInvitationByIdOrNull(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)).thenReturn(invitation)

            mvc
                .post(getLocalCouncilCancelInviteRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_INVITATION_ID)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl("../${getCancelInviteConfirmationRoute(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)}")
                    }
                }

            verify(emailNotificationService)
                .sendEmail(invitation.invitedEmail, LocalCouncilInvitationCancellationEmail(invitation.invitingCouncil))
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `cancelInvitation returns 404 if the invite is not in the database`() {
            whenever(localCouncilInvitationService.getInvitationByIdOrNull(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)).thenReturn(null)
            mvc
                .get(getLocalCouncilCancelInviteRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_INVITATION_ID))
                .andExpect { status { isNotFound() } }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `cancelInvitationSuccess returns 200 if the invitation was cancelled this session`() {
            val deletedInvitation = createLocalCouncilInvitation(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)

            whenever(localCouncilDataService.getInvitationCancelledThisSessionById(deletedInvitation.id))
                .thenReturn(deletedInvitation)

            setupDefaultLocalCouncilForLaAdmin()

            mvc
                .get(getLocalCouncilCancelInviteSuccessRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_INVITATION_ID))
                .andExpect { status { isOk() } }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `cancelInvitationSuccess returns 404 if the invite is not found in the session`() {
            whenever(localCouncilDataService.getInvitationCancelledThisSessionById(DEFAULT_LOCAL_COUNCIL_INVITATION_ID))
                .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

            mvc.get(getLocalCouncilCancelInviteSuccessRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_INVITATION_ID)).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `cancelInvitationSuccess returns 500 if the invitation is still in the database`() {
            whenever(localCouncilDataService.getInvitationCancelledThisSessionById(DEFAULT_LOCAL_COUNCIL_INVITATION_ID))
                .thenThrow(ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))

            mvc.get(getLocalCouncilCancelInviteSuccessRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_INVITATION_ID)).andExpect {
                status { isInternalServerError() }
            }
        }

        private fun setupInvitationPostToCancelInvitationAndAssertSuccess() {
            val invitation = createLocalCouncilInvitation(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)
            whenever(localCouncilInvitationService.getInvitationByIdOrNull(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)).thenReturn(invitation)

            mvc
                .post(getLocalCouncilCancelInviteRoute(DEFAULT_LOCAL_COUNCIL_ID, DEFAULT_LOCAL_COUNCIL_INVITATION_ID)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl("../${getCancelInviteConfirmationRoute(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)}")
                    }
                }

            verify(localCouncilInvitationService).deleteInvitation(DEFAULT_LOCAL_COUNCIL_INVITATION_ID)

            verify(localCouncilDataService).addCancelledInvitationToSession(invitation)
        }
    }

    private fun setupLocalCouncilForSystemOperator(laId: Int = DEFAULT_LOCAL_COUNCIL_ID): LocalCouncil {
        val localCouncil = createLocalCouncil(id = laId)
        whenever(localCouncilService.retrieveLocalCouncilById(laId))
            .thenReturn(localCouncil)

        return localCouncil
    }

    private fun setupDefaultLocalCouncilForLaAdmin(): LocalCouncil {
        val loggedInUserModel = createdLoggedInUserModel()
        val localCouncil = createLocalCouncil()
        whenever(localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(DEFAULT_LOCAL_COUNCIL_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localCouncil))

        return localCouncil
    }

    private fun setupLocalCouncilUserToEdit(
        localCouncil: LocalCouncil,
        laUserId: Long = DEFAULT_LOCAL_COUNCIL_USER_ID,
    ): LocalCouncilUser {
        val baseUser = createOneLoginUser("user")
        val localCouncilUser = createLocalCouncilUser(baseUser, localCouncil, laUserId)
        whenever(localCouncilDataService.getLocalCouncilUserIfAuthorizedLocalCouncil(laUserId, localCouncil.id))
            .thenReturn(
                localCouncilUser,
            )

        return localCouncilUser
    }
}
