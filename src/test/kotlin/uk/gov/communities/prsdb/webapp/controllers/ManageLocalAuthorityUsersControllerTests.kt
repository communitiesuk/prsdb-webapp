package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.any
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
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalAuthorityUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityInvitationCancellationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_INVITATION_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_USER_ID
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
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

    @Test
    fun `index returns a redirect for unauthenticated user`() {
        mvc.get("/local-authority/1/manage-users").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `index returns 403 for unauthorized user`() {
        mvc
            .get("/local-authority/1/manage-users")
            .andExpect {
                status { isForbidden() }
            }
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
            .get("/local-authority/$DEFAULT_LA_ID/manage-users")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `index returns 403 for admin user accessing another LA`() {
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenThrow(AccessDeniedException(""))

        mvc
            .get("/local-authority/${DEFAULT_LA_ID}/manage-users")
            .andExpect {
                status { isForbidden() }
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
            .get("/local-authority/$DEFAULT_LA_ID/manage-users?page=0")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `inviting new user with valid form redirects to confirmation page`() {
        val loggedInUserModel = createdLoggedInUserModel()
        val localAuthority = LocalAuthority(DEFAULT_LA_ID, "Test Local Authority", "custodian code")
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localAuthority))
        whenever(localAuthorityInvitationService.createInvitationToken(any(), any(), any()))
            .thenReturn("test-token")
        whenever(absoluteUrlProvider.buildInvitationUri("test-token"))
            .thenReturn(URI("https://test-service.gov.uk/sign-up-la-user"))

        mvc
            .post("/local-authority/123/invite-new-user") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = urlEncodedConfirmedEmailDataModel("new-user@example.com")
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl("invite-new-user/success")
                flash { attribute("invitedEmailAddress", "new-user@example.com") }
            }
    }

    private fun urlEncodedConfirmedEmailDataModel(
        @Suppress("SameParameterValue") testEmail: String,
    ): String {
        val encodedTestEmail = URLEncoder.encode(testEmail, "UTF-8")
        return "email=$encodedTestEmail&confirmEmail=$encodedTestEmail"
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `getEditUserAccessLevelPage returns 403 for admin user accessing another LA`() {
        createdLoggedInUserModel()
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenThrow(AccessDeniedException(""))

        mvc
            .get("/local-authority/$DEFAULT_LA_ID/edit-user/1")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `getEditUserAccessLevelPage returns 404 for admin user accessing a LA user that does not exist or is from another LA`() {
        val loggedInUserModel = createdLoggedInUserModel()
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localAuthority))
        whenever(localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(DEFAULT_LA_USER_ID, DEFAULT_LA_ID))
            .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

        mvc
            .get("/local-authority/$DEFAULT_LA_ID/edit-user/$DEFAULT_LA_USER_ID")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `getEditUserAccessLevelPage returns 404 for admin user specifying a non-number for the user id`() {
        mvc
            .get("/local-authority/$DEFAULT_LA_ID/edit-user/not-a-number")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `getEditUserAccessLevelPage returns 403 for admin user accessing their own edit page`() {
        val loggedInUserModel = createdLoggedInUserModel()
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localAuthority))
        whenever(localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(loggedInUserModel.id, DEFAULT_LA_ID))
            .thenReturn(loggedInUserModel)

        mvc
            .get("/local-authority/$DEFAULT_LA_ID/edit-user/${loggedInUserModel.id}")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `getEditUserAccessLevelPage returns 200 for admin user accessing a user from its LA`() {
        val loggedInUserModel = createdLoggedInUserModel()
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localAuthority))
        val baseUser = createOneLoginUser("user")
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        whenever(localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(DEFAULT_LA_USER_ID, DEFAULT_LA_ID))
            .thenReturn(
                LocalAuthorityUserDataModel(
                    DEFAULT_LA_USER_ID,
                    localAuthorityUser.name,
                    localAuthority.name,
                    localAuthorityUser.isManager,
                    localAuthorityUser.email,
                ),
            )

        mvc
            .get("/local-authority/$DEFAULT_LA_ID/edit-user/$DEFAULT_LA_USER_ID")
            .andExpect {
                status { isOk() }
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
            .post("/local-authority/$DEFAULT_LA_ID/edit-user/${loggedInUserModel.id}") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = "isManager=false"
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `updateUserAccessLevel updates the given user's access level`() {
        val loggedInUserModel = createdLoggedInUserModel()
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localAuthority))

        mvc
            .post("/local-authority/$DEFAULT_LA_ID/edit-user/$DEFAULT_LA_USER_ID") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = "isManager=true"
                with(csrf())
            }.andExpect {
                status {
                    is3xxRedirection()
                    redirectedUrl("/local-authority/$DEFAULT_LA_ID/manage-users")
                }
            }

        verify(localAuthorityDataService).updateUserAccessLevel(
            LocalAuthorityUserAccessLevelRequestModel(true),
            DEFAULT_LA_USER_ID,
        )
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `confirmDeleteUser gives a 200 for admins of the LA containing the user`() {
        val loggedInUserModel = createdLoggedInUserModel()
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localAuthority))
        val baseUser = createOneLoginUser("user")
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        whenever(localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(DEFAULT_LA_USER_ID, DEFAULT_LA_ID))
            .thenReturn(
                LocalAuthorityUserDataModel(
                    DEFAULT_LA_USER_ID,
                    localAuthorityUser.name,
                    localAuthority.name,
                    localAuthorityUser.isManager,
                    localAuthorityUser.email,
                ),
            )

        mvc
            .get("/local-authority/$DEFAULT_LA_ID/delete-user/$DEFAULT_LA_USER_ID")
            .andExpect {
                status { isOk() }
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
            .post("/local-authority/$DEFAULT_LA_ID/delete-user/${loggedInUserModel.id}") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = "isManager=false"
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `deleteUser deletes the specified user`() {
        val loggedInUserModel = createdLoggedInUserModel()
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localAuthority))
        val baseUser = createOneLoginUser("user")
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        whenever(localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(DEFAULT_LA_USER_ID, DEFAULT_LA_ID))
            .thenReturn(
                LocalAuthorityUserDataModel(
                    DEFAULT_LA_USER_ID,
                    localAuthorityUser.name,
                    localAuthority.name,
                    localAuthorityUser.isManager,
                    localAuthorityUser.email,
                ),
            )

        mvc
            .post("/local-authority/$DEFAULT_LA_ID/delete-user/$DEFAULT_LA_USER_ID") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status {
                    is3xxRedirection()
                    redirectedUrl("../delete-user/success")
                }
            }

        verify(localAuthorityDataService).deleteUser(DEFAULT_LA_USER_ID)
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `deleteUser gives a 403 if attempting to remove the current user`() {
        val loggedInUserModel = createdLoggedInUserModel()
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localAuthority))

        mvc
            .post("/local-authority/$DEFAULT_LA_ID/delete-user/${loggedInUserModel.id}") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `confirmCancelInvitation returns a 200 for admins of the inviting LA`() {
        val loggedInUserModel = createdLoggedInUserModel()
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localAuthority))

        val invitation = createLocalAuthorityInvitation()
        whenever(localAuthorityInvitationService.getInvitationById(DEFAULT_LA_INVITATION_ID)).thenReturn(invitation)

        mvc
            .get("/local-authority/$DEFAULT_LA_ID/cancel-invitation/$DEFAULT_LA_INVITATION_ID")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `confirmCancelInvitation returns 403 for admin user accessing another LA`() {
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenThrow(AccessDeniedException(""))

        val invitation = createLocalAuthorityInvitation()
        whenever(localAuthorityInvitationService.getInvitationById(DEFAULT_LA_INVITATION_ID)).thenReturn(invitation)

        mvc
            .get("/local-authority/$DEFAULT_LA_ID/cancel-invitation/$DEFAULT_LA_INVITATION_ID")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `confirmCancelInvitation returns 403 for admin user accessing an invitation from another LA`() {
        val loggedInUserModel = createdLoggedInUserModel()
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(Pair(loggedInUserModel, localAuthority))

        val invitation = createLocalAuthorityInvitation(localAuthorityId = 789)
        whenever(localAuthorityInvitationService.getInvitationById(DEFAULT_LA_INVITATION_ID)).thenReturn(invitation)

        mvc
            .get("/local-authority/$DEFAULT_LA_ID/cancel-invitation/$DEFAULT_LA_INVITATION_ID")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `cancelInvitation removes the invitation from the database`() {
        val invitation = createLocalAuthorityInvitation()
        whenever(localAuthorityInvitationService.getInvitationById(DEFAULT_LA_INVITATION_ID)).thenReturn(invitation)

        mvc
            .post("/local-authority/$DEFAULT_LA_ID/cancel-invitation/$DEFAULT_LA_INVITATION_ID") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status {
                    is3xxRedirection()
                    redirectedUrl("../cancel-invitation/success")
                }
            }

        verify(localAuthorityInvitationService).deleteInvitation(DEFAULT_LA_INVITATION_ID)
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `cancelInvitation emails a cancellation notification to the invited email address`() {
        val invitation = createLocalAuthorityInvitation()
        whenever(localAuthorityInvitationService.getInvitationById(DEFAULT_LA_INVITATION_ID)).thenReturn(invitation)

        mvc
            .post("/local-authority/$DEFAULT_LA_ID/cancel-invitation/$DEFAULT_LA_INVITATION_ID") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status {
                    is3xxRedirection()
                    redirectedUrl("../cancel-invitation/success")
                }
            }

        verify(emailNotificationService)
            .sendEmail(invitation.invitedEmail, LocalAuthorityInvitationCancellationEmail(invitation.invitingAuthority))
    }
}
