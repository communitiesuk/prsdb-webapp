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
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.INVITE_USER_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getCancelInviteConfirmationRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getDeleteUserConfirmationRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getLaCancelInviteRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getLaCancelInviteSuccessRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getLaDeleteUserRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getLaDeleteUserSuccessRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getLaEditUserRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getLaInviteNewUserRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getLaInviteUserSuccessRoute
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController.Companion.getLaManageUsersRoute
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalAuthorityUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityInvitationCancellationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_INVITATION_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_USER_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LOGGED_IN_LA_USER_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.NON_ADMIN_LA_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthorityUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createdLoggedInUserModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockOneLoginUserData.Companion.createOneLoginUser
import java.net.URI
import java.net.URLEncoder
import kotlin.test.Test

@WebMvcTest(ManageLocalAuthorityUsersController::class)
class ManageLocalAuthorityUsersControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var emailNotificationService: EmailNotificationService<EmailTemplateModel>

    @MockitoBean
    lateinit var localAuthorityInvitationService: LocalAuthorityInvitationService

    @MockitoBean
    lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @MockitoBean
    lateinit var localAuthorityService: LocalAuthorityService

    @MockitoBean
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

    @MockitoBean
    private lateinit var securityContextService: SecurityContextService

    @Nested
    inner class ManageUsersPage {
        @Test
        fun `index returns a redirect for unauthenticated user`() {
            mvc.get(getLaManageUsersRoute(DEFAULT_LA_ID)).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `index returns 403 for unauthorized user`() {
            mvc
                .get(getLaManageUsersRoute(DEFAULT_LA_ID))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_USER"])
        fun `index returns 403 for a local authority (non-admin) user`() {
            mvc
                .get(getLaManageUsersRoute(DEFAULT_LA_ID))
                .andExpect {
                    status { isForbidden() }
                }

            verify(localAuthorityDataService, never()).getPaginatedUsersAndInvitations(any(), any(), anyOrNull(), anyOrNull())
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `index returns 200 for authorized user`() {
            val loggedInUserModel = createdLoggedInUserModel()
            val localAuthority = LocalAuthority(DEFAULT_LA_ID, "Test Local Authority", "custodianCode")
            whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localAuthority))
            whenever(localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 0))
                .thenReturn(PageImpl(listOf(), PageRequest.of(0, 10), 1))

            mvc
                .get(getLaManageUsersRoute(DEFAULT_LA_ID))
                .andExpect {
                    status { isOk() }
                    model {
                        attribute("currentUserId", loggedInUserModel.id)
                        attribute("localAuthority", localAuthority)
                    }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `index returns 403 for admin user accessing another LA`() {
            whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
                .thenThrow(AccessDeniedException(""))

            mvc
                .get(getLaManageUsersRoute(DEFAULT_LA_ID))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `index returns 200 for a system operator`() {
            val localAuthority = setupLocalAuthorityForSystemOperator(NON_ADMIN_LA_ID)
            whenever(localAuthorityDataService.getPaginatedUsersAndInvitations(eq(localAuthority), eq(0), anyOrNull(), anyOrNull()))
                .thenReturn(PageImpl(listOf(), PageRequest.of(0, 10), 1))
            mvc
                .get(getLaManageUsersRoute(NON_ADMIN_LA_ID))
                .andExpect {
                    status { isOk() }
                    model {
                        attribute("localAuthority", localAuthority)
                    }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `index returns 404 for authorized user accessing a page less than 1`() {
            val loggedInUserModel = createdLoggedInUserModel()
            val localAuthority = LocalAuthority(DEFAULT_LA_ID, "Test Local Authority", "custodianCode")
            whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localAuthority))
            whenever(localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 0))
                .thenReturn(PageImpl(listOf(), PageRequest.of(0, 10), 1))

            mvc
                .get("${getLaManageUsersRoute(DEFAULT_LA_ID)}?page=0")
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    @Nested
    inner class InviteNewUser {
        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `sendInvitation as an la admin creates an invitation token`() {
            val invitedEmail = "new-user@example.com"

            val localAuthority = setupSuccessfulPostToSendInvitationAndReturnLocalCouncil()

            postToSendInvitationAndAssertRedirectionToConfirmation(DEFAULT_LA_ID, invitedEmail)

            verify(localAuthorityInvitationService).createInvitationToken(invitedEmail, localAuthority, false)
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `sendInvitation emails the invited user`() {
            val invitationUrl = "https://test-service.gov.uk/sign-up-la-user"
            val dashboardUrl = "https://test-service.gov.uk"
            val localAuthority =
                setupSuccessfulPostToSendInvitationAndReturnLocalCouncil(
                    invitationUrl = invitationUrl,
                    dashboardUrl = dashboardUrl,
                )

            val invitedEmail = "new-user@example.com"

            postToSendInvitationAndAssertRedirectionToConfirmation(DEFAULT_LA_ID, invitedEmail)

            verify(emailNotificationService)
                .sendEmail(
                    invitedEmail,
                    LocalAuthorityInvitationEmail(
                        localAuthority,
                        URI(invitationUrl),
                        dashboardUrl,
                    ),
                )
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `sendInvitation emails admins when a new user is invited`() {
            val localAuthority = setupSuccessfulPostToSendInvitationAndReturnLocalCouncil()

            val invitedEmail = "new-user@example.com"

            postToSendInvitationAndAssertRedirectionToConfirmation(DEFAULT_LA_ID, invitedEmail)

            verify(localAuthorityDataService)
                .sendUserInvitedEmailsToAdmins(localAuthority, invitedEmail)
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `sendInvitation adds the invited user to the session`() {
            val localAuthority = setupSuccessfulPostToSendInvitationAndReturnLocalCouncil()

            val invitedEmail = "new-user@example.com"

            postToSendInvitationAndAssertRedirectionToConfirmation(DEFAULT_LA_ID, invitedEmail)

            verify(localAuthorityDataService).addInvitedLocalAuthorityUserToSession(localAuthority.id, invitedEmail)
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `sendInvitation as an la admin with valid form redirects to confirmation page`() {
            val invitedEmail = "new-user@example.com"

            setupSuccessfulPostToSendInvitationAndReturnLocalCouncil()

            postToSendInvitationAndAssertRedirectionToConfirmation(DEFAULT_LA_ID, invitedEmail)
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `sendInvitation as a system operator with valid form creates the tokens, sends emails and redirects as for an LA admin`() {
            val invitationUrl = "https://test-service.gov.uk/sign-up-la-user"
            val dashboardUrl = "https://test-service.gov.uk"
            val localAuthority =
                setupSuccessfulPostToSendInvitationAndReturnLocalCouncil(
                    userIsSystemOperator = true,
                    invitationUrl = invitationUrl,
                    dashboardUrl = dashboardUrl,
                )

            val invitedEmail = "new-user@example.com"

            postToSendInvitationAndAssertRedirectionToConfirmation(NON_ADMIN_LA_ID, invitedEmail)

            verify(localAuthorityInvitationService).createInvitationToken(invitedEmail, localAuthority, false)

            verify(emailNotificationService)
                .sendEmail(
                    invitedEmail,
                    LocalAuthorityInvitationEmail(
                        localAuthority,
                        URI(invitationUrl),
                        dashboardUrl,
                    ),
                )
            verify(localAuthorityDataService)
                .sendUserInvitedEmailsToAdmins(localAuthority, invitedEmail)
            verify(localAuthorityDataService).addInvitedLocalAuthorityUserToSession(localAuthority.id, invitedEmail)
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `inviteNewUserConfirmation returns 200 if a user was invited to the requested local authority this session`() {
            setupDefaultLocalAuthorityForLaAdmin()
            whenever((localAuthorityDataService.getLastLocalAuthorityUserInvitedThisSession(DEFAULT_LA_ID)))
                .thenReturn("invited.email@example.com")

            mvc.get(getLaInviteUserSuccessRoute(DEFAULT_LA_ID)).andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `inviteNewUserConfirmation returns 404 if no user was invited to the requested local authority this session`() {
            whenever((localAuthorityDataService.getLastUserIdRegisteredThisSession())).thenReturn(null)

            mvc.get(getLaInviteUserSuccessRoute(DEFAULT_LA_ID)).andExpect {
                status { isNotFound() }
            }
        }

        private fun setupSuccessfulPostToSendInvitationAndReturnLocalCouncil(
            userIsSystemOperator: Boolean = false,
            invitationUrl: String = "https://test-service.gov.uk/sign-up-la-user",
            dashboardUrl: String = "https://test-service.gov.uk",
        ): LocalAuthority {
            whenever(localAuthorityInvitationService.createInvitationToken(any(), any(), any()))
                .thenReturn("test-token")
            whenever(absoluteUrlProvider.buildInvitationUri("test-token"))
                .thenReturn(URI(invitationUrl))
            whenever(absoluteUrlProvider.buildLocalAuthorityDashboardUri()).thenReturn(URI(dashboardUrl))

            return if (userIsSystemOperator) {
                setupLocalAuthorityForSystemOperator(NON_ADMIN_LA_ID)
            } else {
                setupDefaultLocalAuthorityForLaAdmin()
            }
        }

        private fun postToSendInvitationAndAssertRedirectionToConfirmation(
            laId: Int = DEFAULT_LA_ID,
            invitedEmail: String,
        ) {
            mvc
                .post(getLaInviteNewUserRoute(laId)) {
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
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `getEditUserAccessLevelPage returns 403 for admin user accessing another LA`() {
            createdLoggedInUserModel()
            whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
                .thenThrow(AccessDeniedException(""))

            mvc
                .get(getLaEditUserRoute(DEFAULT_LA_ID, 1))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `getEditUserAccessLevelPage returns 404 for admin user accessing a LA user that does not exist or is from another LA`() {
            setupDefaultLocalAuthorityForLaAdmin()
            whenever(localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(DEFAULT_LA_USER_ID, DEFAULT_LA_ID))
                .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

            mvc
                .get(getLaEditUserRoute(DEFAULT_LA_ID, DEFAULT_LA_USER_ID))
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `getEditUserAccessLevelPage returns 404 for admin user specifying a non-number for the user id`() {
            mvc
                .get("/$LOCAL_AUTHORITY_PATH_SEGMENT/$DEFAULT_LA_ID/$EDIT_USER_PATH_SEGMENT/not-a-number")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `getEditUserAccessLevelPage returns 403 for admin user accessing their own edit page`() {
            val loggedInUser = createLocalAuthorityUser(name = "Logged In User")
            val loggedInUserModel = LocalAuthorityUserDataModel.fromLocalAuthorityUser(loggedInUser)
            val localAuthority = createLocalAuthority()
            whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localAuthority))
            whenever(localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(loggedInUserModel.id, DEFAULT_LA_ID))
                .thenReturn(loggedInUser)

            mvc
                .get(getLaEditUserRoute(DEFAULT_LA_ID, loggedInUserModel.id))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `getEditUserAccessLevelPage returns 200 for admin user accessing a user from its LA`() {
            val localAuthority = setupDefaultLocalAuthorityForLaAdmin()

            setupLocalAuthorityUserToEdit(localAuthority)

            mvc
                .get(getLaEditUserRoute(DEFAULT_LA_ID, DEFAULT_LA_USER_ID))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("localAuthorityUser", "options") }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `getEditUserAccessLevelPage returns 200 for system operator`() {
            val localAuthority = setupLocalAuthorityForSystemOperator(NON_ADMIN_LA_ID)

            setupLocalAuthorityUserToEdit(localAuthority)

            mvc
                .get(getLaEditUserRoute(NON_ADMIN_LA_ID, DEFAULT_LA_USER_ID))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("localAuthorityUser", "options") }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `updateUserAccessLevel gives a 403 when trying to update currently logged in user`() {
            val loggedInUserModel = createdLoggedInUserModel()
            val localAuthority = createLocalAuthority()
            whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localAuthority))

            mvc
                .post(getLaEditUserRoute(DEFAULT_LA_ID, loggedInUserModel.id)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "isManager=false"
                    with(csrf())
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `updateUserAccessLevel updates the given user's access level when called by an la admin`() {
            setupDefaultLocalAuthorityForLaAdmin()

            postUpdateUserAccessLevelAndAssertSuccess(DEFAULT_LA_ID)
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `updateUserAccessLevel updates the given user's access level when called by a system operator`() {
            setupLocalAuthorityForSystemOperator(NON_ADMIN_LA_ID)

            postUpdateUserAccessLevelAndAssertSuccess()
        }

        private fun postUpdateUserAccessLevelAndAssertSuccess(laId: Int = DEFAULT_LA_ID) {
            mvc
                .post(getLaEditUserRoute(laId, DEFAULT_LA_USER_ID)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "isManager=true"
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl(getLaManageUsersRoute(DEFAULT_LA_ID))
                    }
                }

            verify(localAuthorityDataService).updateUserAccessLevel(
                LocalAuthorityUserAccessLevelRequestModel(true),
                DEFAULT_LA_USER_ID,
            )
        }
    }

    @Nested
    inner class DeleteUser {
        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `confirmDeleteUser gives a 200 for admins of the LA containing the user`() {
            val localAuthority = setupDefaultLocalAuthorityForLaAdmin()
            setupLocalAuthorityUserToEdit(localAuthority)

            mvc
                .get(getLaDeleteUserRoute(DEFAULT_LA_ID, DEFAULT_LA_USER_ID))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("user") }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `confirmDeleteUser gives a 200 for system operators`() {
            val localAuthority = setupLocalAuthorityForSystemOperator(NON_ADMIN_LA_ID)
            setupLocalAuthorityUserToEdit(localAuthority)

            mvc
                .get(getLaDeleteUserRoute(NON_ADMIN_LA_ID, DEFAULT_LA_USER_ID))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("user") }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `confirmDeleteUser gives a 403 when trying to delete currently logged in user`() {
            val loggedInUserModel = createdLoggedInUserModel()
            val localAuthority = createLocalAuthority()
            whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localAuthority))

            mvc
                .post(getLaDeleteUserRoute(DEFAULT_LA_ID, loggedInUserModel.id)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    content = "isManager=false"
                    with(csrf())
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `deleteUser deletes the specified user for an la admin and adds their details to the session`() {
            val localAuthority = setupDefaultLocalAuthorityForLaAdmin()

            val user = setupLocalAuthorityUserToEdit(localAuthority)

            postDeleteUserAndAssertSuccess(DEFAULT_LA_ID, user)

            verify(localAuthorityDataService).addDeletedUserToSession(user)
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `deleteUser deletes the specified user for a system operator and adds their details to the session`() {
            val localAuthority = setupLocalAuthorityForSystemOperator(NON_ADMIN_LA_ID)

            val user = setupLocalAuthorityUserToEdit(localAuthority)

            postDeleteUserAndAssertSuccess(NON_ADMIN_LA_ID, user)

            verify(localAuthorityDataService).addDeletedUserToSession(user)
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `deleteUser returns 403 if a user who is not a system operator attempts to delete themself`() {
            val loggedInUserModel = createdLoggedInUserModel()
            val localAuthority = createLocalAuthority()
            whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
                .thenReturn(Pair(loggedInUserModel, localAuthority))

            mvc
                .post(getLaDeleteUserRoute(DEFAULT_LA_ID, loggedInUserModel.id)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR", "LA_ADMIN"])
        fun `deleteUser allows a system operator to delete themself then refreshes the user roles`() {
            val localAuthority = setupLocalAuthorityForSystemOperator(DEFAULT_LA_ID)
            val user = setupLocalAuthorityUserToEdit(localAuthority, DEFAULT_LOGGED_IN_LA_USER_ID)
            whenever(localAuthorityDataService.getLocalAuthorityUser("user")).thenReturn(user)

            postDeleteUserAndAssertSuccess(DEFAULT_LA_ID, user)

            verify(securityContextService).refreshContext()
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR", "LA_ADMIN"])
        fun `deleteUser does not refresh user roles if the logged in user was not deleted`() {
            val localAuthority = setupLocalAuthorityForSystemOperator(DEFAULT_LA_ID)
            val user = setupLocalAuthorityUserToEdit(localAuthority, DEFAULT_LA_USER_ID)
            val loggedInUser = createLocalAuthorityUser(id = DEFAULT_LOGGED_IN_LA_USER_ID)
            whenever(localAuthorityDataService.getLocalAuthorityUser("user")).thenReturn(loggedInUser)

            postDeleteUserAndAssertSuccess(DEFAULT_LA_ID, user)

            verify(securityContextService, never()).refreshContext()
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `deleteUserSuccess returns 200 if the user was deleted this session`() {
            setupDefaultLocalAuthorityForLaAdmin()
            whenever(localAuthorityDataService.getUsersDeletedThisSession())
                .thenReturn(mutableListOf(createLocalAuthorityUser(id = DEFAULT_LA_USER_ID)))
            whenever(localAuthorityDataService.getLocalAuthorityUserOrNull(DEFAULT_LA_USER_ID)).thenReturn(null)

            mvc
                .get(getLaDeleteUserSuccessRoute(DEFAULT_LA_ID, DEFAULT_LA_USER_ID))
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `deleteUserSuccess returns 404 if the user was not deleted in this session`() {
            mvc
                .get(getLaDeleteUserSuccessRoute(DEFAULT_LA_ID, DEFAULT_LA_USER_ID))
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `delete user success page returns 500 if the user is still in the database`() {
            val userBeingDeleted = createLocalAuthorityUser(id = DEFAULT_LA_USER_ID)
            whenever(localAuthorityDataService.getUsersDeletedThisSession())
                .thenReturn(mutableListOf(userBeingDeleted))
            whenever(localAuthorityDataService.getLocalAuthorityUserOrNull(userBeingDeleted.id)).thenReturn(userBeingDeleted)

            mvc
                .get(getLaDeleteUserSuccessRoute(DEFAULT_LA_ID, DEFAULT_LA_USER_ID))
                .andExpect {
                    status { is5xxServerError() }
                }
        }

        private fun postDeleteUserAndAssertSuccess(
            laId: Int = DEFAULT_LA_ID,
            userBeingDeleted: LocalAuthorityUser = createLocalAuthorityUser(id = DEFAULT_LA_USER_ID),
        ): LocalAuthorityUser {
            whenever(localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(userBeingDeleted.id, laId))
                .thenReturn(userBeingDeleted)

            mvc
                .post(getLaDeleteUserRoute(laId, userBeingDeleted.id)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl("../${getDeleteUserConfirmationRoute(userBeingDeleted.id)}")
                    }
                }

            verify(localAuthorityDataService).deleteUser(userBeingDeleted)

            return userBeingDeleted
        }
    }

    @Nested
    inner class CancelInvitation {
        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `confirmCancelInvitation returns a 200 for admins of the inviting LA`() {
            setupDefaultLocalAuthorityForLaAdmin()

            val invitation = createLocalAuthorityInvitation()
            whenever(localAuthorityInvitationService.getInvitationByIdOrNull(DEFAULT_LA_INVITATION_ID)).thenReturn(invitation)

            mvc
                .get(getLaCancelInviteRoute(DEFAULT_LA_ID, DEFAULT_LA_INVITATION_ID))
                .andExpect {
                    status { isOk() }
                    model { attribute("email", invitation.invitedEmail) }
                }
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `confirmCancelInvitation returns a 200 for system operator`() {
            setupLocalAuthorityForSystemOperator(NON_ADMIN_LA_ID)

            val invitation = createLocalAuthorityInvitation(invitingAuthority = createLocalAuthority(id = NON_ADMIN_LA_ID))
            whenever(localAuthorityInvitationService.getInvitationByIdOrNull(DEFAULT_LA_INVITATION_ID)).thenReturn(invitation)

            mvc
                .get(getLaCancelInviteRoute(NON_ADMIN_LA_ID, DEFAULT_LA_INVITATION_ID))
                .andExpect {
                    status { isOk() }
                    model { attribute("email", invitation.invitedEmail) }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `confirmCancelInvitation returns 403 for admin user accessing another LA`() {
            whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
                .thenThrow(AccessDeniedException(""))

            val invitation = createLocalAuthorityInvitation()
            whenever(localAuthorityInvitationService.getInvitationByIdOrNull(DEFAULT_LA_INVITATION_ID)).thenReturn(invitation)

            mvc
                .get(getLaCancelInviteRoute(DEFAULT_LA_ID, DEFAULT_LA_INVITATION_ID))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `confirmCancelInvitation returns 403 for admin user accessing an invitation from another LA`() {
            setupDefaultLocalAuthorityForLaAdmin()

            val invitation = createLocalAuthorityInvitation(invitingAuthority = createLocalAuthority(id = 789))
            whenever(localAuthorityInvitationService.getInvitationByIdOrNull(DEFAULT_LA_INVITATION_ID)).thenReturn(invitation)

            mvc
                .get(getLaCancelInviteRoute(DEFAULT_LA_ID, DEFAULT_LA_INVITATION_ID))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `cancelInvitation returns 404 if the invitation is not in the database`() {
            whenever(localAuthorityInvitationService.getInvitationByIdOrNull(DEFAULT_LA_INVITATION_ID)).thenReturn(null)

            mvc
                .post(getLaCancelInviteRoute(DEFAULT_LA_ID, DEFAULT_LA_INVITATION_ID)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `cancelInvitation removes the invitation from the database and adds it to the session when called by an la admin`() {
            setupInvitationPostToCancelInvitationAndAssertSuccess()
        }

        @Test
        @WithMockUser(roles = ["SYSTEM_OPERATOR"])
        fun `cancelInvitation removes the invitation from the database and adds it to the session when called by a system operator`() {
            setupInvitationPostToCancelInvitationAndAssertSuccess()
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `cancelInvitation emails a cancellation notification to the invited email address`() {
            val invitation = createLocalAuthorityInvitation()
            whenever(localAuthorityInvitationService.getInvitationByIdOrNull(DEFAULT_LA_INVITATION_ID)).thenReturn(invitation)

            mvc
                .post(getLaCancelInviteRoute(DEFAULT_LA_ID, DEFAULT_LA_INVITATION_ID)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl("../${getCancelInviteConfirmationRoute(DEFAULT_LA_INVITATION_ID)}")
                    }
                }

            verify(emailNotificationService)
                .sendEmail(invitation.invitedEmail, LocalAuthorityInvitationCancellationEmail(invitation.invitingAuthority))
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `cancelInvitation returns 404 if the invite is not in the database`() {
            whenever(localAuthorityInvitationService.getInvitationByIdOrNull(DEFAULT_LA_INVITATION_ID)).thenReturn(null)
            mvc
                .get(getLaCancelInviteRoute(DEFAULT_LA_ID, DEFAULT_LA_INVITATION_ID))
                .andExpect { status { isNotFound() } }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `cancelInvitationSuccess returns 200 if the invitation was cancelled this session`() {
            val deletedInvitation = createLocalAuthorityInvitation(DEFAULT_LA_INVITATION_ID)

            whenever(localAuthorityDataService.getInvitationsCancelledThisSession())
                .thenReturn(mutableListOf(deletedInvitation))

            setupDefaultLocalAuthorityForLaAdmin()

            mvc
                .get(getLaCancelInviteSuccessRoute(DEFAULT_LA_ID, DEFAULT_LA_INVITATION_ID))
                .andExpect { status { isOk() } }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `cancelInvitationSuccess returns 404 if the invite is not found in the session`() {
            whenever(localAuthorityDataService.getInvitationsCancelledThisSession())
                .thenReturn(mutableListOf(createLocalAuthorityInvitation(DEFAULT_LA_INVITATION_ID + 1)))

            mvc.get(getLaCancelInviteSuccessRoute(DEFAULT_LA_ID, DEFAULT_LA_INVITATION_ID)).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `cancelInvitationSuccess returns 500 if the invitation is still in the database`() {
            val deletedInvitation = createLocalAuthorityInvitation(DEFAULT_LA_INVITATION_ID)
            whenever(localAuthorityDataService.getInvitationsCancelledThisSession())
                .thenReturn(mutableListOf(deletedInvitation))

            whenever(localAuthorityInvitationService.getInvitationByIdOrNull(DEFAULT_LA_INVITATION_ID))
                .thenReturn(deletedInvitation)

            mvc.get(getLaCancelInviteSuccessRoute(DEFAULT_LA_ID, DEFAULT_LA_INVITATION_ID)).andExpect {
                status { isInternalServerError() }
            }
        }

        private fun setupInvitationPostToCancelInvitationAndAssertSuccess() {
            val invitation = createLocalAuthorityInvitation(DEFAULT_LA_INVITATION_ID)
            whenever(localAuthorityInvitationService.getInvitationByIdOrNull(DEFAULT_LA_INVITATION_ID)).thenReturn(invitation)

            mvc
                .post(getLaCancelInviteRoute(DEFAULT_LA_ID, DEFAULT_LA_INVITATION_ID)) {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    with(csrf())
                }.andExpect {
                    status {
                        is3xxRedirection()
                        redirectedUrl("../${getCancelInviteConfirmationRoute(DEFAULT_LA_INVITATION_ID)}")
                    }
                }

            verify(localAuthorityInvitationService).deleteInvitation(DEFAULT_LA_INVITATION_ID)

            verify(localAuthorityDataService).addCancelledInvitationToSession(invitation)
        }
    }

    private fun setupLocalAuthorityForSystemOperator(laId: Int = DEFAULT_LA_ID): LocalAuthority {
        val localAuthority = createLocalAuthority(id = laId)
        whenever(localAuthorityService.retrieveLocalAuthorityById(laId))
            .thenReturn(localAuthority)

        return localAuthority
    }

    private fun setupDefaultLocalAuthorityForLaAdmin(): LocalAuthority {
        val loggedInUserModel = createdLoggedInUserModel()
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localAuthority))

        return localAuthority
    }

    private fun setupLocalAuthorityUserToEdit(
        localAuthority: LocalAuthority,
        laUserId: Long = DEFAULT_LA_USER_ID,
    ): LocalAuthorityUser {
        val baseUser = createOneLoginUser("user")
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority, laUserId)
        whenever(localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(laUserId, localAuthority.id))
            .thenReturn(
                localAuthorityUser,
            )

        return localAuthorityUser
    }
}
