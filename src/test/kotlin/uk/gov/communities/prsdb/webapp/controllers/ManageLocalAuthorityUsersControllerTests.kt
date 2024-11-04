package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
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
    fun `ManageLocalAuthorityUsersController returns a redirect for unauthenticated user`() {
        mvc.get("/local-authority/1/manage-users").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `ManageLocalAuthorityUsersController returns 403 for unauthorized user`() {
        mvc
            .get("/local-authority/1/manage-users")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `ManageLocalAuthorityUsersController returns 200 for authorized user`() {
        val localAuthority = LocalAuthority(123, "Test Local Authority")
        whenever(localAuthorityDataService.getLocalAuthorityForUser("user"))
            .thenReturn(localAuthority)
        whenever(localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 0))
            .thenReturn(PageImpl(listOf(), PageRequest.of(0, 10), 0))

        mvc
            .get("/local-authority/123/manage-users")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `ManageLocalAuthorityUsersController returns 403 for admin user accessing another LA`() {
        val localAuthority = LocalAuthority(123, "Test Local Authority")
        whenever(localAuthorityDataService.getLocalAuthorityForUser("user"))
            .thenReturn(localAuthority)
        whenever(localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 0))
            .thenReturn(PageImpl(listOf(), PageRequest.of(0, 10), 0))

        mvc
            .get("/local-authority/456/manage-users")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `inviting new user with valid form redirects to confirmation page`() {
        val localAuthority = LocalAuthority(123, "Test Local Authority")
        whenever(localAuthorityDataService.getLocalAuthorityForUser("user"))
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
}
