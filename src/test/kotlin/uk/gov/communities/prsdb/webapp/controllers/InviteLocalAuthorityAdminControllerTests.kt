package uk.gov.communities.prsdb.webapp.controllers

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
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import java.net.URLEncoder
import kotlin.test.Test

@WebMvcTest(InviteLocalAuthorityAdminController::class)
class InviteLocalAuthorityAdminControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var localAuthorityService: LocalAuthorityService

    @Test
    fun `inviteLocalAuthorityAdmin redirects unauthenticated users`() {
        mvc.get("/system-operator/invite-la-admin").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `inviteLocalAuthorityAdmin returns 403 for unauthorized users`() {
        mvc.get("/system-operator/invite-la-admin").andExpect {
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

        mvc.get("/system-operator/invite-la-admin").andExpect {
            status { isOk() }
            model { attributeExists("selectOptions", "inviteLocalAuthorityAdminModel") }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendInvitation redirects to success page for valid form submission`() {
        mvc
            .post("/system-operator/invite-la-admin") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = urlEncodedInviteLocalAuthorityAdminModel("new-user@example.com", MockLocalAuthorityData.DEFAULT_LA_ID)
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl("/system-operator/invite-la-admin/success")
            }
    }

    private fun urlEncodedInviteLocalAuthorityAdminModel(
        @Suppress("SameParameterValue") testEmail: String,
        @Suppress("SameParameterValue") localAuthorityId: Int,
    ): String {
        val encodedTestEmail = URLEncoder.encode(testEmail, "UTF-8")
        return "email=$encodedTestEmail&confirmEmail=$encodedTestEmail&localAuthorityId=$localAuthorityId"
    }
}
