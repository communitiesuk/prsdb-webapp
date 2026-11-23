package uk.gov.communities.prsdb.webapp.controllers

import kotlinx.serialization.SerializationException
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.json.JSONException
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_EMAIL
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_SMS
import uk.gov.communities.prsdb.webapp.controllers.GeneratePassphraseController.Companion.GENERATE_PASSPHRASE_URL
import uk.gov.communities.prsdb.webapp.exceptions.EmailNotifyResponseException
import uk.gov.communities.prsdb.webapp.exceptions.NotificationSendFailureReason
import uk.gov.communities.prsdb.webapp.exceptions.NotifyAllowlistException
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.SmsSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SendPassphraseEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SendPassphraseSmsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PassphraseEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.PassphraseSms
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
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
    private lateinit var emailNotificationService: EmailNotificationService<PassphraseEmail>

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
                param("status", "generatePassphrase.email.accepted")
            }.andExpect {
                view { name("generatePassphrase") }
                model { attribute("passphrase", "time anything costume") }
                content { string(not(containsString("client supplied content"))) }
            }
    }

    @ParameterizedTest
    @ValueSource(strings = ["sms", "email"])
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `accepted submissions render a separate success page`(channel: String) {
        val recipient = if (channel == "sms") "07400123456" else "recipient@example.com"
        mvc
            .get(GENERATE_PASSPHRASE_URL) {
                flashAttrs =
                    mapOf(
                        "attemptedPassphrase" to "time anything costume",
                        "status" to "generatePassphrase.$channel.accepted",
                        "recipient" to recipient,
                    )
            }.andExpect {
                status { isOk() }
                view { name("generatePassphraseSubmitted") }
                model {
                    attribute("passphrase", "time anything costume")
                    attribute("generatePassphraseUrl", GENERATE_PASSPHRASE_URL)
                    attributeDoesNotExist("smsFormModel", "emailFormModel")
                }
                content {
                    string(containsString("govuk-notification-banner--success"))
                    string(containsString("has been submitted to $recipient."))
                    string(containsString("Ask the landlord to read the passphrase back when it arrives."))
                    string(not(containsString("offensive or inappropriate")))
                    string(not(containsString("role=\"status\"")))
                    string(not(containsString("passphrase-letter-guidance")))
                }
            }

        verifyNoInteractions(passphraseService, smsNotificationService, emailNotificationService)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "sms.invalid", "sms.technical", "sms.dailyLimit", "sms.minuteLimit", "sms.unknown",
            "email.invalid", "email.technical", "email.dailyLimit", "email.minuteLimit", "email.unknown",
        ],
    )
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `failed or uncertain submissions do not render a success page`(outcome: String) {
        mvc
            .get(GENERATE_PASSPHRASE_URL) {
                flashAttrs =
                    mapOf(
                        "attemptedPassphrase" to "time anything costume",
                        "status" to "generatePassphrase.$outcome",
                    )
            }.andExpect {
                status { isOk() }
                view { name("generatePassphrase") }
                model { attribute("passphrase", "time anything costume") }
                content {
                    string(containsString("role=\"status\""))
                    string(not(containsString("govuk-notification-banner--success")))
                    string(not(containsString("Use this passphrase in a letter")))
                    string(not(containsString("Send text message")))
                    string(not(containsString("Send email")))
                }
            }

        verifyNoInteractions(passphraseService, smsNotificationService, emailNotificationService)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `generatePassphrase hides SMS sending when the flag is disabled`() {
        whenever(passphraseService.generatePassphrase()).thenReturn("time anything costume")

        mvc.get(GENERATE_PASSPHRASE_URL).andExpect {
            content {
                string(not(containsString("Send text message")))
                string(containsString("Use this passphrase in a letter"))
                string(containsString("long-lived secret"))
                string(not(containsString("What happens next")))
            }
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
                string(containsString("action=\"$GENERATE_PASSPHRASE_URL\""))
                string(containsString("Use this passphrase in a letter"))
                string(containsString("long-lived secret"))
                string(not(containsString("What happens next")))
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["07400 123456", "+33 6 12 34 56 78", "+1 202 555 0123", "+447700900001", "+447700900002", " 07400123456 "])
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms sends the displayed phrase and ignores client supplied message content`(phoneNumber: String) {
        val (session, token) = generateSmsForm()

        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("channel", "sms")
                param("phoneNumber", phoneNumber)
                param("passphraseToken", token)
                param("passphrase", "client supplied content")
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(GENERATE_PASSPHRASE_URL)
                flash {
                    attribute("attemptedPassphrase", "time anything costume")
                    attribute("status", "generatePassphrase.sms.accepted")
                    attribute("recipient", phoneNumber.trim())
                }
            }

        verify(smsNotificationService).sendSms(phoneNumber.trim(), PassphraseSms("time anything costume"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "not a number", "+44", "020 7946 0123", "+33 1 42 68 53 00"])
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms retains the phrase and form token when the number is invalid`(phoneNumber: String) {
        val (session, token) = generateSmsForm()

        val result =
            mvc
                .post(GENERATE_PASSPHRASE_URL) {
                    this.session = session
                    with(csrf())
                    param("channel", "sms")
                    param("phoneNumber", phoneNumber)
                    param("passphraseToken", token)
                }.andExpect {
                    status { isOk() }
                    view { name("generatePassphrase") }
                    model {
                        attribute("passphrase", "time anything costume")
                        attributeHasFieldErrors("smsFormModel", "phoneNumber")
                    }
                    content { string(containsString("Error: New passphrase")) }
                }.andReturn()

        assertEquals(token, (result.modelAndView!!.model["smsFormModel"] as SendPassphraseSmsFormModel).passphraseToken)
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
                param("channel", "sms")
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
                param("channel", "sms")
                param("phoneNumber", "07400123456")
                param("passphraseToken", token)
            }.andExpect {
                status { is3xxRedirection() }
                flash { attribute("status", "generatePassphrase.expired") }
            }

        verifyNoInteractions(smsNotificationService)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms rejects resubmitting the same form after an uncertain failure`() {
        val (session, token) = generateSmsForm()
        doThrow(SmsSendException(NotificationSendFailureReason.UNKNOWN, "Unknown outcome"))
            .whenever(smsNotificationService)
            .sendSms(any(), any())

        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("channel", "sms")
                param("phoneNumber", "07400123456")
                param("passphraseToken", token)
            }.andExpect {
                flash { attribute("status", "generatePassphrase.sms.unknown") }
            }
        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("channel", "sms")
                param("phoneNumber", "07400123456")
                param("passphraseToken", token)
            }.andExpect {
                flash { attribute("status", "generatePassphrase.expired") }
            }

        verify(smsNotificationService).sendSms(any(), any())
    }

    @ParameterizedTest
    @CsvSource(
        "INVALID_REQUEST, invalid, Check the telephone number",
        "TECHNICAL, technical, Contact support",
        "DAILY_LIMIT, dailyLimit, sent again tomorrow",
        "MINUTE_LIMIT, minuteLimit, Wait a short time",
        "UNKNOWN, unknown, Ask the landlord if it arrives",
    )
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms reports the reason without retrying`(
        reason: NotificationSendFailureReason,
        status: String,
        guidance: String,
    ) {
        val (session, token) = generateSmsForm()
        doThrow(SmsSendException(reason, "Simulated failure"))
            .whenever(smsNotificationService)
            .sendSms(any(), any())

        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("channel", "sms")
                param("phoneNumber", "07400123456")
                param("passphraseToken", token)
            }.andExpect {
                flash {
                    attribute("status", "generatePassphrase.sms.$status")
                    attribute("attemptedPassphrase", "time anything costume")
                }
            }

        verify(smsNotificationService).sendSms(eq("07400123456"), any())
        mvc.get(GENERATE_PASSPHRASE_URL) {
            flashAttrs = mapOf("attemptedPassphrase" to "time anything costume", "status" to "generatePassphrase.sms.$status")
        }.andExpect {
            content { string(containsString(guidance)) }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendSms requires a CSRF token`() {
        enableSms()
        mvc.post(GENERATE_PASSPHRASE_URL) {
            param("channel", "sms")
        }.andExpect { status { isForbidden() } }
        verifyNoInteractions(smsNotificationService)
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `sendSms forbids non system operators`() {
        enableSms()
        mvc.post(GENERATE_PASSPHRASE_URL) {
            with(csrf())
            param("channel", "sms")
        }.andExpect { status { isForbidden() } }
        verifyNoInteractions(smsNotificationService)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "letter"])
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sending a passphrase requires a supported channel`(channel: String) {
        val (session, token) = generateSmsForm()

        mvc.post(GENERATE_PASSPHRASE_URL) {
            this.session = session
            with(csrf())
            if (channel.isNotEmpty()) {
                param("channel", channel)
            }
            param("phoneNumber", "07400123456")
            param("passphraseToken", token)
        }.andExpect { status { isBadRequest() } }

        verifyNoInteractions(smsNotificationService, emailNotificationService)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `generatePassphrase shows only email sending when only its flag is enabled`() {
        enableEmail()

        mvc.get(GENERATE_PASSPHRASE_URL).andExpect {
            content {
                string(containsString("Send email"))
                string(not(containsString("Send text message")))
                string(containsString("_csrf"))
                string(containsString("Use this passphrase in a letter"))
            }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `generatePassphrase hides email sending when only SMS is enabled`() {
        enableSms()

        mvc.get(GENERATE_PASSPHRASE_URL).andExpect {
            content { string(not(containsString("Send email"))) }
        }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendEmail synchronously sends the displayed phrase and ignores client supplied content`() {
        val (session, token) = generateEmailForm()

        mvc
            .post(GENERATE_PASSPHRASE_URL) {
                this.session = session
                with(csrf())
                param("channel", "email")
                param("emailAddress", "recipient@example.com")
                param("passphraseToken", token)
                param("passphrase", "client supplied content")
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(GENERATE_PASSPHRASE_URL)
                flash {
                    attribute("attemptedPassphrase", "time anything costume")
                    attribute("status", "generatePassphrase.email.accepted")
                    attribute("recipient", "recipient@example.com")
                }
            }

        verify(emailNotificationService).sendEmailSynchronously("recipient@example.com", PassphraseEmail("time anything costume"))
        verifyNoInteractions(smsNotificationService)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendEmail confirms the submitted recipient as escaped text`() {
        val (session, token) = generateEmailForm()
        val result =
            mvc
                .post(GENERATE_PASSPHRASE_URL) {
                    this.session = session
                    with(csrf())
                    param("channel", "email")
                    param("emailAddress", "first&last@example.com")
                    param("recipient", "client-supplied@example.com")
                    param("passphraseToken", token)
                }.andExpect {
                    status { is3xxRedirection() }
                    flash { attribute("recipient", "first&last@example.com") }
                }.andReturn()

        mvc
            .get(GENERATE_PASSPHRASE_URL) {
                flashAttrs = result.flashMap
                param("recipient", "client-supplied@example.com")
            }.andExpect {
                content {
                    string(containsString("The email has been submitted to first&amp;last@example.com."))
                    string(not(containsString("client-supplied@example.com")))
                }
            }

        verify(emailNotificationService).sendEmailSynchronously("first&last@example.com", PassphraseEmail("time anything costume"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "not an email", "name@invalid"])
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `invalid email retains both send forms with the same phrase and token`(emailAddress: String) {
        enableSms()
        val (session, token) = generateEmailForm()

        val result =
            mvc
                .post(GENERATE_PASSPHRASE_URL) {
                    this.session = session
                    with(csrf())
                    param("channel", "email")
                    param("emailAddress", emailAddress)
                    param("passphraseToken", token)
                }.andExpect {
                    status { isOk() }
                    model {
                        attribute("passphrase", "time anything costume")
                        attributeHasFieldErrors("emailFormModel", "emailAddress")
                    }
                    content {
                        string(containsString("Error: New passphrase"))
                        string(containsString("Send email"))
                        string(containsString("Send text message"))
                    }
                }.andReturn()

        val model = result.modelAndView!!.model
        assertEquals(token, (model["emailFormModel"] as SendPassphraseEmailFormModel).passphraseToken)
        assertEquals(token, (model["smsFormModel"] as SendPassphraseSmsFormModel).passphraseToken)
        verifyNoInteractions(emailNotificationService, smsNotificationService)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `an attempt on either channel consumes the token for both channels`(emailFirst: Boolean) {
        enableSms()
        val (session, token) = generateEmailForm()
        mvc.post(GENERATE_PASSPHRASE_URL) {
            this.session = session
            with(csrf())
            param("channel", if (emailFirst) "email" else "sms")
            param("emailAddress", "recipient@example.com")
            param("phoneNumber", "07400123456")
            param("passphraseToken", token)
        }.andExpect { status { is3xxRedirection() } }

        mvc.post(GENERATE_PASSPHRASE_URL) {
            this.session = session
            with(csrf())
            param("channel", if (emailFirst) "sms" else "email")
            param("emailAddress", "recipient@example.com")
            param("phoneNumber", "07400123456")
            param("passphraseToken", token)
        }.andExpect {
            flash {
                attribute("status", "generatePassphrase.expired")
            }
        }

        if (emailFirst) {
            verify(emailNotificationService).sendEmailSynchronously(any(), any())
            verifyNoInteractions(smsNotificationService)
        } else {
            verify(smsNotificationService).sendSms(any(), any())
            verifyNoInteractions(emailNotificationService)
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "invalid", "technical", "daily", "minute", "uncertain-transient", "uncertain-persistent",
            "validation", "allowlist", "serialization", "json", "invalid-response",
        ],
    )
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `email failures consume the token without retry or fallback`(failure: String) {
        val (session, token) = generateEmailForm()
        val exception =
            when (failure) {
                "invalid" -> PersistentEmailSendException(NotificationSendFailureReason.INVALID_REQUEST, "Invalid request")
                "technical" -> PersistentEmailSendException(NotificationSendFailureReason.TECHNICAL, "Technical problem")
                "daily" -> PersistentEmailSendException(NotificationSendFailureReason.DAILY_LIMIT, "Daily limit")
                "minute" -> TransientEmailSentException(NotificationSendFailureReason.MINUTE_LIMIT, "Rate limited")
                "uncertain-transient" -> TransientEmailSentException(NotificationSendFailureReason.UNKNOWN, "Unknown outcome")
                "validation" ->
                    EmailNotifyResponseException(
                        NotificationSendFailureReason.INVALID_REQUEST,
                        "Invalid request",
                        IllegalArgumentException(),
                    )
                "allowlist" -> NotifyAllowlistException(NotificationSendFailureReason.TECHNICAL, "Not allowed")
                "serialization" -> SerializationException("Unreadable error")
                "json" -> JSONException("Unreadable response")
                "invalid-response" -> IllegalArgumentException("Invalid response")
                else -> PersistentEmailSendException(NotificationSendFailureReason.UNKNOWN, "Unknown outcome")
            }
        doThrow(exception).whenever(emailNotificationService).sendEmailSynchronously(any(), any())

        mvc.post(GENERATE_PASSPHRASE_URL) {
            this.session = session
            with(csrf())
            param("channel", "email")
            param("emailAddress", "recipient@example.com")
            param("passphraseToken", token)
        }.andExpect {
            status { is3xxRedirection() }
            flash {
                attribute(
                    "status",
                    when (failure) {
                        "invalid", "validation" -> "generatePassphrase.email.invalid"
                        "technical", "allowlist" -> "generatePassphrase.email.technical"
                        "daily" -> "generatePassphrase.email.dailyLimit"
                        "minute" -> "generatePassphrase.email.minuteLimit"
                        else -> "generatePassphrase.email.unknown"
                    },
                )
            }
        }
        mvc.post(GENERATE_PASSPHRASE_URL) {
            this.session = session
            with(csrf())
            param("channel", "email")
            param("emailAddress", "recipient@example.com")
            param("passphraseToken", token)
        }.andExpect {
            flash { attribute("status", "generatePassphrase.expired") }
        }

        verify(emailNotificationService).sendEmailSynchronously("recipient@example.com", PassphraseEmail("time anything costume"))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendEmail rejects a stale form after another phrase is generated`() {
        val (session, token) = generateEmailForm()
        mvc.get(GENERATE_PASSPHRASE_URL) { this.session = session }

        mvc.post(GENERATE_PASSPHRASE_URL) {
            this.session = session
            with(csrf())
            param("channel", "email")
            param("emailAddress", "recipient@example.com")
            param("passphraseToken", token)
        }.andExpect {
            flash { attribute("status", "generatePassphrase.expired") }
        }
        verifyNoInteractions(emailNotificationService)
    }

    @Test
    fun `sendEmail redirects unauthenticated users`() {
        mvc.post(GENERATE_PASSPHRASE_URL) {
            with(csrf())
            param("channel", "email")
        }.andExpect { status { is3xxRedirection() } }
        verifyNoInteractions(emailNotificationService)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "unknown-token"])
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendEmail rejects a missing or unknown token`(token: String) {
        enableEmail()

        mvc.post(GENERATE_PASSPHRASE_URL) {
            with(csrf())
            param("channel", "email")
            param("emailAddress", "recipient@example.com")
            param("passphraseToken", token)
        }.andExpect {
            flash { attribute("status", "generatePassphrase.expired") }
        }
        verifyNoInteractions(emailNotificationService)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `sendEmail requires a CSRF token`() {
        enableEmail()
        mvc.post(GENERATE_PASSPHRASE_URL) {
            param("channel", "email")
        }.andExpect { status { isForbidden() } }
        verifyNoInteractions(emailNotificationService)
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `sendEmail forbids non system operators`() {
        mvc.post(GENERATE_PASSPHRASE_URL) {
            with(csrf())
            param("channel", "email")
        }.andExpect { status { isForbidden() } }
        verifyNoInteractions(emailNotificationService)
    }

    private fun enableEmail() {
        whenever(featureFlagManager.checkFeature(PASSPHRASE_EMAIL)).thenReturn(true)
        whenever(passphraseService.generatePassphrase()).thenReturn("time anything costume")
    }

    private fun generateEmailForm(): Pair<MockHttpSession, String> {
        enableEmail()
        val session = MockHttpSession()
        val result = mvc.get(GENERATE_PASSPHRASE_URL) { this.session = session }.andReturn()
        val form = result.modelAndView!!.model["emailFormModel"] as SendPassphraseEmailFormModel
        return session to form.passphraseToken
    }

    private fun enableSms() {
        whenever(featureFlagManager.checkFeature(PASSPHRASE_SMS)).thenReturn(true)
        whenever(passphraseService.generatePassphrase()).thenReturn("time anything costume")
    }

    private fun generateSmsForm(): Pair<MockHttpSession, String> {
        enableSms()
        val session = MockHttpSession()
        val result = mvc.get(GENERATE_PASSPHRASE_URL) { this.session = session }.andReturn()
        val form = result.modelAndView!!.model["smsFormModel"] as SendPassphraseSmsFormModel
        return session to form.passphraseToken
    }
}
