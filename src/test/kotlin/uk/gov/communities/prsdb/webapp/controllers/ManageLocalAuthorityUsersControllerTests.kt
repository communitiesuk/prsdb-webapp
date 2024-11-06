package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_ID
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_USER_ID
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthorityUser
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createOneLoginUser
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserAccessLevelDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import java.net.URI
import java.net.URLEncoder
import kotlin.test.Test

@WebMvcTest(ManageLocalAuthorityUsersController::class)
class ManageLocalAuthorityUsersControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    lateinit var emailNotificationService: EmailNotificationService<EmailTemplateModel>

    @MockBean
    lateinit var localAuthorityInvitationService: LocalAuthorityInvitationService

    @MockBean
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
    fun `ManageLocalAuthorityUsersController returns 200 for authorized user`() {
        val localAuthority = LocalAuthority(DEFAULT_LA_ID, "Test Local Authority")
        whenever(localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(localAuthority)
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
        whenever(localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenThrow(AccessDeniedException(""))

        mvc
            .get("/local-authority/${DEFAULT_LA_ID}/manage-users")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `inviting new user with valid form redirects to confirmation page`() {
        val localAuthority = LocalAuthority(DEFAULT_LA_ID, "Test Local Authority")
        whenever(localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(localAuthority)
        whenever(localAuthorityInvitationService.createInvitationToken(any(), any()))
            .thenReturn("test-token")
        whenever(localAuthorityInvitationService.buildInvitationUri("test-token"))
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
        whenever(localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
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
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(localAuthority)
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
    fun `getEditUserAccessLevelPage returns 200 for admin user accessing a user from its LA`() {
        val localAuthority = createLocalAuthority()
        whenever(localAuthorityDataService.getLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, "user"))
            .thenReturn(localAuthority)
        val baseUser = createOneLoginUser("user")
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        whenever(localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(DEFAULT_LA_USER_ID, DEFAULT_LA_ID))
            .thenReturn(
                LocalAuthorityUserDataModel(
                    DEFAULT_LA_USER_ID,
                    baseUser.name,
                    localAuthority.name,
                    localAuthorityUser.isManager,
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
    fun `updateUserAccessLevel updates the given user's access level`() {
        mvc
            .post("/local-authority/$DEFAULT_LA_ID/edit-user/$DEFAULT_LA_USER_ID") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = "isManager=true"
                with(csrf())
            }.andExpect {
                status {
                    status { is3xxRedirection() }
                    redirectedUrl("/local-authority/$DEFAULT_LA_ID/manage-users")
                }
            }

        verify(localAuthorityDataService).updateUserAccessLevel(
            LocalAuthorityUserAccessLevelDataModel(true),
            DEFAULT_LA_USER_ID,
        )
    }
}
