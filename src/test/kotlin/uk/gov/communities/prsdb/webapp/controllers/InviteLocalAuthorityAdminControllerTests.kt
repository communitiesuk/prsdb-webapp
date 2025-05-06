package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityAdminInvitationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import java.net.URI
import java.net.URLEncoder
import kotlin.test.Test

@WebMvcTest(InviteLocalAuthorityAdminController::class)
class InviteLocalAuthorityAdminControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var localAuthorityService: LocalAuthorityService

    @MockitoBean
    lateinit var emailNotificationService: EmailNotificationService<EmailTemplateModel>

    @MockitoBean
    lateinit var localAuthorityInvitationService: LocalAuthorityInvitationService

    @MockitoBean
    lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @Test
    fun `inviteLocalAuthorityAdmin redirects unauthenticated users`() {
        mvc.get(InviteLocalAuthorityAdminController.INVITE_LA_ADMIN_ROUTE).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `inviteLocalAuthorityAdmin returns 403 for unauthorized users`() {
        mvc.get(InviteLocalAuthorityAdminController.INVITE_LA_ADMIN_ROUTE).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `inviteLocalAuthorityAdmin returns 200 for authorized users`() {
        val localAuthorities = listOf(createLocalAuthority())
        whenever(localAuthorityService.retrieveAllLocalAuthorities()).thenReturn(
            localAuthorities,
        )

        mvc.get(InviteLocalAuthorityAdminController.INVITE_LA_ADMIN_ROUTE).andExpect {
            status { isOk() }
            model { attributeExists("selectOptions", "inviteLocalAuthorityAdminModel") }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendInvitation redirects to success page for valid form submission`() {
        val testEmail = "new-user@example.com"

        whenever(localAuthorityInvitationService.createInvitationToken(any(), any(), any()))
            .thenReturn("test-token")
        whenever(absoluteUrlProvider.buildInvitationUri("test-token"))
            .thenReturn(URI("https://test-service.gov.uk/sign-up-la-user"))
        whenever(localAuthorityService.retrieveLocalAuthorityById(MockLocalAuthorityData.DEFAULT_LA_ID))
            .thenReturn(createLocalAuthority(MockLocalAuthorityData.DEFAULT_LA_ID))

        mvc
            .post(InviteLocalAuthorityAdminController.INVITE_LA_ADMIN_ROUTE) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = urlEncodedInviteLocalAuthorityAdminModel(testEmail, MockLocalAuthorityData.DEFAULT_LA_ID)
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl("${InviteLocalAuthorityAdminController.INVITE_LA_ADMIN_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendInvitation sends invitation for valid form submission`() {
        val testEmail = "new-user@example.com"
        val localAuthority = createLocalAuthority(MockLocalAuthorityData.DEFAULT_LA_ID)
        val invitationUri = URI("https://test-service.gov.uk/sign-up-la-user")
        whenever(localAuthorityInvitationService.createInvitationToken(any(), any(), any()))
            .thenReturn("test-token")
        whenever(absoluteUrlProvider.buildInvitationUri("test-token"))
            .thenReturn(invitationUri)
        whenever(localAuthorityService.retrieveLocalAuthorityById(MockLocalAuthorityData.DEFAULT_LA_ID))
            .thenReturn(localAuthority)

        mvc
            .post(InviteLocalAuthorityAdminController.INVITE_LA_ADMIN_ROUTE) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = urlEncodedInviteLocalAuthorityAdminModel(testEmail, MockLocalAuthorityData.DEFAULT_LA_ID)
                with(csrf())
            }.andExpect {
                flash { attribute("invitedEmailAddress", testEmail) }
            }

        verify(emailNotificationService).sendEmail(testEmail, LocalAuthorityAdminInvitationEmail(localAuthority, invitationUri))
    }

    private fun urlEncodedInviteLocalAuthorityAdminModel(
        @Suppress("SameParameterValue") testEmail: String,
        @Suppress("SameParameterValue") localAuthorityId: Int,
    ): String {
        val encodedTestEmail = URLEncoder.encode(testEmail, "UTF-8")
        return "email=$encodedTestEmail&confirmEmail=$encodedTestEmail&localAuthorityId=$localAuthorityId"
    }
}
