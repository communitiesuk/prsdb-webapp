package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(LocalCouncilPrivacyNoticeController::class)
class LocalCouncilPrivacyNoticeControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Test
    fun `LocalCouncilPrivacyNoticeController returns 200 for unauthenticated user`() {
        mvc.get(LocalCouncilPrivacyNoticeController.LOCAL_AUTHORITY_PRIVACY_NOTICE_ROUTE).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `LocalCouncilPrivacyNoticeController returns 308 for unauthenticated user with trailing slash`() {
        mvc.get("${LocalCouncilPrivacyNoticeController.LOCAL_AUTHORITY_PRIVACY_NOTICE_ROUTE}/").andExpect {
            status { isPermanentRedirect() }
        }
    }

    @Test
    @WithMockUser
    fun `LocalCouncilPrivacyNoticeController returns 200 for authenticated user`() {
        mvc.get(LocalCouncilPrivacyNoticeController.LOCAL_AUTHORITY_PRIVACY_NOTICE_ROUTE).andExpect {
            status { isOk() }
        }
    }
}
