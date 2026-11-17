package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.MessageSourceConfig
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_SMS
import uk.gov.communities.prsdb.webapp.controllers.GeneratePassphraseController.Companion.GENERATE_PASSPHRASE_URL
import uk.gov.communities.prsdb.webapp.exceptions.PersistentSmsSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientSmsSendException
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SendPassphraseSmsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.PassphraseSms
import uk.gov.communities.prsdb.webapp.services.PassphraseService
import uk.gov.communities.prsdb.webapp.services.SmsNotificationService
import kotlin.test.Test
import kotlin.test.assertEquals

@WebMvcTest(GeneratePassphraseController::class)
@Import(MessageSourceConfig::class)
class GeneratePassphraseControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var passphraseService: PassphraseService

    @MockitoBean
    private lateinit var smsNotificationService: SmsNotificationService<PassphraseSms>

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    @Test
    fun `generatePassphrase redirects unauthenticated users`() {
        mvc
            .get(GENERATE_PASSPHRASE_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `generatePassphrase returns 403 for users who are not system operators`() {
        mvc
            .get(GENERATE_PASSPHRASE_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `generatePassphrase returns the generated passphrase for a system operator`() {
        whenever(passphraseService.generatePassphrase()).thenReturn("time anything costume")

        mvc
            .get(GENERATE_PASSPHRASE_URL)
            .andExpect {
                status { isOk() }
                view { name("generatePassphrase") }
                model {
                    attribute("passphrase", "time anything costume")
                    attribute("generatePassphraseUrl", GENERATE_PASSPHRASE_URL)
                }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `generatePassphrase renders the generated passphrase`() {
        whenever(passphraseService.generatePassphrase()).thenReturn("time anything costume")

        mvc
            .get(GENERATE_PASSPHRASE_URL)
            .andExpect {
                status { isOk() }
                content {
                    string(containsString("New passphrase"))
                    string(containsString("The passphrase is"))
                    string(containsString("time anything costume"))
                    string(containsString("Use this passphrase in a letter"))
                }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `generatePassphrase ignores passphrases and submission status supplied in query parameters`() {
        whenever(passphraseService.generatePassphrase()).thenReturn("time anything costume")

        mvc
            .get(GENERATE_PASSPHRASE_URL) {
                param("passphrase", "client supplied content")
                param("attemptedPassphrase", "client supplied content")
                param("smsStatus", "generatePassphrase.sms.accepted")
            }.andExpect {
                view { name("generatePassphrase") }
                model { attribute("passphrase", "time anything costume") }
                content { string(not(containsString("client supplied content"))) }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `accepted SMS submissions render a separate success page`() {
        mvc.get(GENERATE_PASSPHRASE_URL) {
            flashAttrs =
                mapOf(
                    "attemptedPassphrase" to "time anything costume",
                    "smsStatus" to "generatePassphrase.sms.accepted",
                    "recipient" to "02079460123",
                )
        }.andExpect {
            status { isOk() }
            view { name("generatePassphraseSubmitted") }
            model {
                attribute("passphrase", "time anything costume")
                attribute("generatePassphraseUrl", GENERATE_PASSPHRASE_URL)
                attributeDoesNotExist("formModel")
            }
            content {
                string(containsString("govuk-notification-banner--success"))
                string(containsString("has been submitted to 02079460123."))
                string(containsString("Ask the landlord to read the passphrase back when it arrives."))
            }
        }

        verifyNoInteractions(passphraseService, smsNotificationService)
    }

    @ParameterizedTest
    @ValueSource(strings = ["sms.failed", "sms.unknown"])
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `failed or uncertain SMS submissions do not render a success page`(outcome: String) {
        mvc.get(GENERATE_PASSPHRASE_URL) {
            flashAttrs = mapOf("attemptedPassphrase" to "time anything costume", "smsStatus" to "generatePassphrase.$outcome")
        }.andExpect {
            status { isOk() }
            view { name("generatePassphrase") }
            model { attribute("passphrase", "time anything costume") }
            content {
                string(containsString("role=\"status\""))
                string(not(containsString("govuk-notification-banner--success")))
            }
        }

        verifyNoInteractions(passphraseService, smsNotificationService)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `generatePassphrase hides SMS sending when the flag is disabled`() {
        whenever(passphraseService.generatePassphrase()).thenReturn("time anything costume")

        mvc.get(GENERATE_PASSPHRASE_URL).andExpect {
            content { string(not(containsString("Send text message"))) }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `generatePassphrase shows the SMS form when enabled`() {
        enableSms()

        mvc.get(GENERATE_PASSPHRASE_URL).andExpect {
            content {
                string(containsString("Send text message"))
                string(containsString("phoneNumber"))
                string(containsString("_csrf"))
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["07400 123456", "020 7946 0123", "+33 1 42 68 53 00", "+447700900001", "+447700900002", " 07400123456 "])
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms sends the displayed phrase and ignores client supplied message content`(phoneNumber: String) {
        val (session, token) = generateSmsForm()

        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("phoneNumber", phoneNumber)
                param("passphraseToken", token)
                param("passphrase", "client supplied content")
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(GENERATE_PASSPHRASE_URL)
                flash {
                    attribute("attemptedPassphrase", "time anything costume")
                    attribute("smsStatus", "generatePassphrase.sms.accepted")
                    attribute("recipient", phoneNumber.trim())
                }
            }

        verify(smsNotificationService).sendSms(phoneNumber.trim(), PassphraseSms("time anything costume"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "not a number", "+44"])
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms retains the phrase and form token when the number is invalid`(phoneNumber: String) {
        val (session, token) = generateSmsForm()

        val result =
            mvc
                .post(GENERATE_PASSPHRASE_URL) {
                    this.session = session
                    with(csrf())
                    param("phoneNumber", phoneNumber)
                    param("passphraseToken", token)
                }.andExpect {
                    status { isOk() }
                    view { name("generatePassphrase") }
                    model {
                        attribute("passphrase", "time anything costume")
                        attributeHasFieldErrors("formModel", "phoneNumber")
                    }
                    content { string(containsString("Error: New passphrase")) }
                }.andReturn()

        assertEquals(token, (result.modelAndView!!.model["formModel"] as SendPassphraseSmsFormModel).passphraseToken)
        verifyNoInteractions(smsNotificationService)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms does not resend a consumed form token`() {
        val (session, token) = generateSmsForm()

        repeat(2) {
            mvc.post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("phoneNumber", "07400123456")
                param("passphraseToken", token)
            }
        }

        verify(smsNotificationService).sendSms("07400123456", PassphraseSms("time anything costume"))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms rejects an old form after generating another phrase`() {
        val (session, token) = generateSmsForm()
        mvc.get(GENERATE_PASSPHRASE_URL) { this.session = session }

        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("phoneNumber", "07400123456")
                param("passphraseToken", token)
            }.andExpect {
                status { is3xxRedirection() }
                flash { attribute("smsStatus", "generatePassphrase.sms.expired") }
            }

        verifyNoInteractions(smsNotificationService)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms rejects resubmitting the same form after an uncertain failure`() {
        val (session, token) = generateSmsForm()
        doThrow(PersistentSmsSendException("Unknown outcome"))
            .whenever(smsNotificationService)
            .sendSms(any(), any())

        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("phoneNumber", "07400123456")
                param("passphraseToken", token)
            }.andExpect {
                flash { attribute("smsStatus", "generatePassphrase.sms.unknown") }
            }
        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("phoneNumber", "07400123456")
                param("passphraseToken", token)
            }.andExpect {
                flash { attribute("smsStatus", "generatePassphrase.sms.expired") }
            }

        verify(smsNotificationService).sendSms(any(), any())
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms reports a temporary rejection without retrying`() {
        val (session, token) = generateSmsForm()
        doThrow(TransientSmsSendException("Rate limited"))
            .whenever(smsNotificationService)
            .sendSms(any(), any())

        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("phoneNumber", "07400123456")
                param("passphraseToken", token)
            }.andExpect {
                flash { attribute("smsStatus", "generatePassphrase.sms.failed") }
            }

        verify(smsNotificationService).sendSms(eq("07400123456"), any())
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms requires a CSRF token`() {
        mvc.post(GENERATE_PASSPHRASE_URL).andExpect { status { isForbidden() } }
        verifyNoInteractions(smsNotificationService)
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `sendSms forbids non system operators`() {
        mvc.post(GENERATE_PASSPHRASE_URL) { with(csrf()) }.andExpect { status { isForbidden() } }
        verifyNoInteractions(smsNotificationService)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms is unavailable when its flag is disabled`() {
        mvc.post(GENERATE_PASSPHRASE_URL) { with(csrf()) }.andExpect { status { isNotFound() } }
        verifyNoInteractions(smsNotificationService)
    }

    private fun enableSms() {
        whenever(featureFlagManager.checkFeature(PASSPHRASE_SMS)).thenReturn(true)
        whenever(passphraseService.generatePassphrase()).thenReturn("time anything costume")
    }

    private fun generateSmsForm(): Pair<MockHttpSession, String> {
        enableSms()
        val session = MockHttpSession()
        val result = mvc.get(GENERATE_PASSPHRASE_URL) { this.session = session }.andReturn()
        val form = result.modelAndView!!.model["formModel"] as SendPassphraseSmsFormModel
        return session to form.passphraseToken
    }
}
