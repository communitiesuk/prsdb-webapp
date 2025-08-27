package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.BetaFeedbackEmail
import uk.gov.communities.prsdb.webapp.services.NotifyEmailNotificationService

@WebMvcTest(BetaFeedbackController::class)
class BetaFeedbackControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var emailNotificationService: NotifyEmailNotificationService<BetaFeedbackEmail>

    @Nested
    inner class LandlordFeedbackTests {
        @Test
        fun `landlordFeedback returns redirect for unauthenticated users`() {
            mvc.get(BetaFeedbackController.LANDLORD_FEEDBACK_URL).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `landlordFeedback returns success for any authenticated user`() {
            mvc.get(BetaFeedbackController.LANDLORD_FEEDBACK_URL).andExpect {
                status { isOk() }
            }
        }

        @Test
        fun `landlordFeedbackSuccess returns redirect for unauthenticated users`() {
            mvc.get(BetaFeedbackController.LANDLORD_FEEDBACK_SUCCESS_URL).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `landlordFeedbackSuccess returns success for any authenticated user`() {
            mvc.get(BetaFeedbackController.LANDLORD_FEEDBACK_SUCCESS_URL).andExpect {
                status { isOk() }
            }
        }
    }

    @Nested
    inner class FeedbackTests {
        @Test
        fun `feedback returns redirect for unauthenticated users`() {
            mvc.get(BetaFeedbackController.FEEDBACK_URL).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `feedback returns success for any authenticated user`() {
            mvc.get(BetaFeedbackController.FEEDBACK_URL).andExpect {
                status { isOk() }
            }
        }

        @Test
        fun `feedbackSuccess returns redirect for unauthenticated users`() {
            mvc.get(BetaFeedbackController.FEEDBACK_SUCCESS_URL).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `feedbackSuccess returns success for any authenticated user`() {
            mvc.get(BetaFeedbackController.FEEDBACK_SUCCESS_URL).andExpect {
                status { isOk() }
            }
        }
    }

    @Nested
    inner class LocalAuthorityFeedbackTests {
        @Test
        fun `localAuthorityFeedback returns redirect for unauthenticated users`() {
            mvc.get(BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_URL).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_USER"])
        fun `localAuthorityFeedback returns success for LA_USER role`() {
            mvc.get(BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_URL).andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `localAuthorityFeedback returns success for LA_ADMIN role`() {
            mvc.get(BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_URL).andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser
        fun `localAuthorityFeedback returns 403 for unauthorized roles`() {
            mvc.get(BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_URL).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `localAuthorityFeedbackSuccess returns redirect for unauthenticated users`() {
            mvc.get(BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_SUCCESS_URL).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_USER"])
        fun `localAuthorityFeedbackSuccess returns success for LA_USER role`() {
            mvc.get(BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_SUCCESS_URL).andExpect {
                status { isOk() }
            }
        }

        @Test
        @WithMockUser(roles = ["LA_ADMIN"])
        fun `localAuthorityFeedbackSuccess returns success for LA_ADMIN role`() {
            mvc.get(BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_SUCCESS_URL).andExpect {
                status { isOk() }
            }
        }
    }
}
