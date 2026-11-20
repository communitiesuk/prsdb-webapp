package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_SMS
import uk.gov.communities.prsdb.webapp.controllers.GeneratePassphraseController.Companion.GENERATE_PASSPHRASE_URL
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SendPassphraseSmsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.PassphraseSms
import uk.gov.communities.prsdb.webapp.services.SmsNotificationService

class GeneratePassphraseControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @MockitoBean
    private lateinit var smsNotificationService: SmsNotificationService<PassphraseSms>

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms is unavailable if the SMS feature flag is disabled`() {
        featureFlagManager.enableFeature(PASSPHRASE_SMS)
        val (session, token) = generateSmsForm()
        featureFlagManager.disableFeature(PASSPHRASE_SMS)

        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("phoneNumber", "07400123456")
                param("passphraseToken", token)
            }.andExpect { status { isNotFound() } }

        verifyNoInteractions(smsNotificationService)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms is available if the SMS feature flag is enabled`() {
        featureFlagManager.enableFeature(PASSPHRASE_SMS)
        val (session, token) = generateSmsForm()
        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("phoneNumber", "07400123456")
                param("passphraseToken", token)
            }.andExpect { status { is3xxRedirection() } }

        verify(smsNotificationService).sendSms(eq("07400123456"), any())
    }

    private fun generateSmsForm(): Pair<MockHttpSession, String> {
        val session = MockHttpSession()
        val result = mvc.get(GENERATE_PASSPHRASE_URL) { this.session = session }.andReturn()
        val form = result.modelAndView!!.model["formModel"] as SendPassphraseSmsFormModel
        return session to form.passphraseToken
    }
}
