package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import kotlinx.serialization.SerializationException
import org.json.JSONException
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.GENERATE_PASSPHRASE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_EMAIL
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_SMS
import uk.gov.communities.prsdb.webapp.constants.PENDING_PASSPHRASE
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.exceptions.EmailNotifyResponseException
import uk.gov.communities.prsdb.webapp.exceptions.NotificationSendFailureReason
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.SmsSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.PendingPassphrase
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SendPassphraseEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SendPassphraseSmsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PassphraseEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.PassphraseSms
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PassphraseService
import uk.gov.communities.prsdb.webapp.services.SmsNotificationService
import java.util.UUID

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(GeneratePassphraseController.GENERATE_PASSPHRASE_URL)
class GeneratePassphraseController(
    private val passphraseService: PassphraseService,
    private val smsNotificationService: SmsNotificationService<PassphraseSms>,
    private val emailNotificationService: EmailNotificationService<PassphraseEmail>,
    private val featureFlagManager: FeatureFlagManager,
) {
    @GetMapping
    fun generatePassphrase(
        model: Model,
        session: HttpSession,
    ): String {
        // The POST supplies this server-side flash attribute after a send attempt, not a query parameter.
        val attemptedPassphrase = model.getAttribute("attemptedPassphrase") as? String
        if (
            attemptedPassphrase != null &&
            model.getAttribute("status") in listOf("generatePassphrase.sms.accepted", "generatePassphrase.email.accepted")
        ) {
            model.addAttribute("passphrase", attemptedPassphrase)
            model.addAttribute("generatePassphraseUrl", GENERATE_PASSPHRASE_URL)
            return "generatePassphraseSubmitted"
        }

        val sendingEnabled =
            featureFlagManager.checkFeature(PASSPHRASE_SMS) || featureFlagManager.checkFeature(PASSPHRASE_EMAIL)
        val passphrase = attemptedPassphrase ?: passphraseService.generatePassphrase()
        val token =
            if (sendingEnabled && attemptedPassphrase == null) {
                UUID.randomUUID().toString().also {
                    session.setAttribute(PENDING_PASSPHRASE, PendingPassphrase(it, passphrase))
                }
            } else {
                ""
            }
        return renderPage(model, passphrase, token)
    }

    @PostMapping(params = ["channel=sms"])
    @AvailableWhenFeatureEnabled(PASSPHRASE_SMS)
    fun sendSms(
        @Valid @ModelAttribute("smsFormModel") smsFormModel: SendPassphraseSmsFormModel,
        bindingResult: BindingResult,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): String =
        sendPassphrase(
            smsFormModel.passphraseToken,
            smsFormModel.phoneNumber.trim(),
            bindingResult,
            model,
            session,
            redirectAttributes,
            send = ::sendSmsNotification,
        )

    @PostMapping(params = ["channel=email"])
    @AvailableWhenFeatureEnabled(PASSPHRASE_EMAIL)
    fun sendEmail(
        @Valid @ModelAttribute("emailFormModel") emailFormModel: SendPassphraseEmailFormModel,
        bindingResult: BindingResult,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): String =
        sendPassphrase(
            emailFormModel.passphraseToken,
            emailFormModel.emailAddress.trim(),
            bindingResult,
            model,
            session,
            redirectAttributes,
            send = ::sendEmailNotification,
        )

    private fun sendPassphrase(
        token: String,
        recipient: String,
        bindingResult: BindingResult,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
        send: (String, String) -> String,
    ): String {
        val pendingPassphrase = session.getAttribute(PENDING_PASSPHRASE) as? PendingPassphrase
        if (pendingPassphrase == null || pendingPassphrase.token != token) {
            redirectAttributes.addFlashAttribute("status", "generatePassphrase.expired")
            return "redirect:$GENERATE_PASSPHRASE_URL"
        }
        val passphrase = pendingPassphrase.passphrase
        if (bindingResult.hasErrors()) {
            return renderPage(model, passphrase, token, hasErrors = true)
        }

        // Consume before sending: an uncertain response must not cause a repeated form submission to resend.
        session.removeAttribute(PENDING_PASSPHRASE)
        val status = send(recipient, passphrase)
        redirectAttributes.addFlashAttribute("status", status)
        redirectAttributes.addFlashAttribute("attemptedPassphrase", passphrase)
        redirectAttributes.addFlashAttribute("recipient", recipient)
        return "redirect:$GENERATE_PASSPHRASE_URL"
    }

    private fun sendSmsNotification(
        phoneNumber: String,
        passphrase: String,
    ): String =
        try {
            smsNotificationService.sendSms(phoneNumber, PassphraseSms(passphrase))
            "generatePassphrase.sms.accepted"
        } catch (exception: SmsSendException) {
            logger.warn("Passphrase SMS submission failed: {}", exception.reason, exception)
            failureStatus("sms", exception.reason)
        }

    private fun sendEmailNotification(
        emailAddress: String,
        passphrase: String,
    ): String =
        try {
            emailNotificationService.sendEmailSynchronously(emailAddress, PassphraseEmail(passphrase))
            "generatePassphrase.email.accepted"
        } catch (exception: Exception) {
            val reason =
                when (exception) {
                    is TransientEmailSentException -> exception.reason
                    is PersistentEmailSendException -> exception.reason
                    is EmailNotifyResponseException -> exception.reason
                    is SerializationException, is JSONException, is IllegalArgumentException -> NotificationSendFailureReason.UNKNOWN
                    else -> throw exception
                }
            logger.warn("Passphrase email submission failed: {}", reason, exception)
            failureStatus("email", reason)
        }

    private fun failureStatus(
        channel: String,
        reason: NotificationSendFailureReason,
    ): String {
        val outcome =
            when (reason) {
                NotificationSendFailureReason.INVALID_REQUEST -> "invalid"
                NotificationSendFailureReason.TECHNICAL -> "technical"
                NotificationSendFailureReason.DAILY_LIMIT -> "dailyLimit"
                NotificationSendFailureReason.MINUTE_LIMIT -> "minuteLimit"
                NotificationSendFailureReason.UNKNOWN -> "unknown"
            }
        return "generatePassphrase.$channel.$outcome"
    }

    private fun renderPage(
        model: Model,
        passphrase: String,
        token: String,
        hasErrors: Boolean = false,
    ): String {
        if (!model.containsAttribute("smsFormModel")) {
            model.addAttribute("smsFormModel", SendPassphraseSmsFormModel().apply { passphraseToken = token })
        }
        if (!model.containsAttribute("emailFormModel")) {
            model.addAttribute("emailFormModel", SendPassphraseEmailFormModel().apply { passphraseToken = token })
        }
        model.addAttribute("passphrase", passphrase)
        model.addAttribute("smsEnabled", featureFlagManager.checkFeature(PASSPHRASE_SMS))
        model.addAttribute("emailEnabled", featureFlagManager.checkFeature(PASSPHRASE_EMAIL))
        model.addAttribute("hasErrors", hasErrors)
        model.addAttribute("generatePassphraseUrl", GENERATE_PASSPHRASE_URL)
        return "generatePassphrase"
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GeneratePassphraseController::class.java)

        const val GENERATE_PASSPHRASE_URL =
            "/$SYSTEM_OPERATOR_PATH_SEGMENT/$GENERATE_PASSPHRASE_PATH_SEGMENT"
    }
}
