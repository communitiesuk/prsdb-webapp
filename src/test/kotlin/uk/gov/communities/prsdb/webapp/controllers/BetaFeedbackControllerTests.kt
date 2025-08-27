package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.BetaFeedbackEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.BetaFeedbackModel
import uk.gov.communities.prsdb.webapp.services.NotifyEmailNotificationService

@WebMvcTest(BetaFeedbackController::class)
class BetaFeedbackControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var emailNotificationService: NotifyEmailNotificationService<BetaFeedbackEmail>

    // TODO: PRSD-1441 - update to environment variable
    val feedbackEmailRecipient = "Team-PRSDB@Softwire.com"

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

        @Test
        @WithMockUser
        fun `submitting landlordFeedback escapes brackets in feedback, emails the team and redirects to the success page`() {
            val betaFeedbackModel =
                BetaFeedbackModel().apply {
                    feedback = "This is a test feedback with brackets () []"
                    email = "test@example.com"
                    referrerHeader = "http://example.com/somepage"
                }

            val expectedEscapedFeedback = "This is a test feedback with brackets \\(\\) \\[\\]"

            submitFeedbackCheckRedirectionAndVerifyEmailSent(
                BetaFeedbackController.LANDLORD_FEEDBACK_URL,
                betaFeedbackModel,
                BetaFeedbackController.LANDLORD_FEEDBACK_SUCCESS_URL,
                expectedEscapedFeedback,
            )
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
        @WithMockUser
        fun `submitting feedback escapes brackets in feedback, emails the team and redirects to the success page`() {
            val betaFeedbackModel =
                BetaFeedbackModel().apply {
                    feedback = "This is a test feedback with brackets () []"
                    email = "test@example.com"
                    referrerHeader = "http://example.com/somepage"
                }

            val expectedEscapedFeedback = "This is a test feedback with brackets \\(\\) \\[\\]"

            submitFeedbackCheckRedirectionAndVerifyEmailSent(
                BetaFeedbackController.FEEDBACK_URL,
                betaFeedbackModel,
                BetaFeedbackController.LANDLORD_FEEDBACK_SUCCESS_URL,
                expectedEscapedFeedback,
            )
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

        @Test
        @WithMockUser(roles = ["LA_USER"])
        fun `submitting localAuthorityFeedback escapes brackets in feedback, emails the team and redirects to the success page`() {
            val betaFeedbackModel =
                BetaFeedbackModel().apply {
                    feedback = "This is a test feedback with brackets () []"
                    email = "test@example.com"
                    referrerHeader = "http://example.com/somepage"
                }

            val expectedEscapedFeedback = "This is a test feedback with brackets \\(\\) \\[\\]"

            submitFeedbackCheckRedirectionAndVerifyEmailSent(
                BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_URL,
                betaFeedbackModel,
                BetaFeedbackController.LOCAL_AUTHORITY_FEEDBACK_SUCCESS_URL,
                expectedEscapedFeedback,
            )
        }
    }

    private fun submitFeedbackCheckRedirectionAndVerifyEmailSent(
        postUrl: String,
        formFeedback: BetaFeedbackModel,
        successUrl: String,
        expectedEscapedFeedback: String,
    ) {
        mvc
            .post(postUrl) {
                param("feedback", formFeedback.feedback)
                param("email", formFeedback.email ?: "")
                param("referrerHeader", formFeedback.referrerHeader ?: "")
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(successUrl)
            }

        verify(emailNotificationService).sendEmail(
            feedbackEmailRecipient,
            BetaFeedbackEmail(
                feedback = expectedEscapedFeedback,
                email = formFeedback.email,
                referrer = formFeedback.referrerHeader,
            ),
        )
    }
}
