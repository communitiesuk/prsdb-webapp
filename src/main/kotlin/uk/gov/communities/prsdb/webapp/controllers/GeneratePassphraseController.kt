package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.GENERATE_PASSPHRASE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_FOR_SMS
import uk.gov.communities.prsdb.webapp.constants.PASSPHRASE_SMS
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.exceptions.PersistentSmsSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientSmsSendException
import uk.gov.communities.prsdb.webapp.models.dataModels.PendingPassphrase
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SendPassphraseSmsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.PassphraseSms
import uk.gov.communities.prsdb.webapp.services.PassphraseService
import uk.gov.communities.prsdb.webapp.services.SmsNotificationService
import java.util.UUID

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(GeneratePassphraseController.GENERATE_PASSPHRASE_URL)
class GeneratePassphraseController(
    private val passphraseService: PassphraseService,
    private val smsNotificationService: SmsNotificationService<PassphraseSms>,
    private val featureFlagManager: FeatureFlagManager,
) {
    @GetMapping
    fun generatePassphrase(
        model: Model,
        session: HttpSession,
    ): String {
        val smsEnabled = featureFlagManager.checkFeature(PASSPHRASE_SMS)
        val formModel = SendPassphraseSmsFormModel()
        // The POST supplies this server-side flash attribute after an SMS attempt, not a query parameter.
        val attemptedPassphrase = model.getAttribute("attemptedPassphrase") as? String
        if (attemptedPassphrase != null && model.getAttribute("smsStatus") == "generatePassphrase.sms.accepted") {
            model.addAttribute("passphrase", attemptedPassphrase)
            model.addAttribute("generatePassphraseUrl", GENERATE_PASSPHRASE_URL)
            return "generatePassphraseSubmitted"
        }
        val passphrase = attemptedPassphrase ?: passphraseService.generatePassphrase()
        if (smsEnabled && attemptedPassphrase == null) {
            formModel.passphraseToken = UUID.randomUUID().toString()
            session.setAttribute(PASSPHRASE_FOR_SMS, PendingPassphrase(formModel.passphraseToken, passphrase))
        }
        model.addAttribute("passphrase", passphrase)
        model.addAttribute("formModel", formModel)
        model.addAttribute("smsEnabled", smsEnabled)
        model.addAttribute("hasErrors", false)
        model.addAttribute("generatePassphraseUrl", GENERATE_PASSPHRASE_URL)
        return "generatePassphrase"
    }

    @PostMapping
    fun sendSms(
        @Valid @ModelAttribute("formModel") formModel: SendPassphraseSmsFormModel,
        bindingResult: BindingResult,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (!featureFlagManager.checkFeature(PASSPHRASE_SMS)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
        val pendingPassphrase = session.getAttribute(PASSPHRASE_FOR_SMS) as? PendingPassphrase
        if (pendingPassphrase == null || pendingPassphrase.token != formModel.passphraseToken) {
            redirectAttributes.addFlashAttribute("smsStatus", "generatePassphrase.sms.expired")
            return "redirect:$GENERATE_PASSPHRASE_URL"
        }
        val passphrase = pendingPassphrase.passphrase
        if (bindingResult.hasErrors()) {
            model.addAttribute("passphrase", passphrase)
            model.addAttribute("smsEnabled", true)
            model.addAttribute("hasErrors", true)
            model.addAttribute("generatePassphraseUrl", GENERATE_PASSPHRASE_URL)
            return "generatePassphrase"
        }

        // Consume before sending: an uncertain response must not cause a repeated form submission to resend.
        session.removeAttribute(PASSPHRASE_FOR_SMS)
        val phoneNumber = formModel.phoneNumber.trim()
        val status =
            try {
                smsNotificationService.sendSms(phoneNumber, PassphraseSms(passphrase))
                redirectAttributes.addFlashAttribute("recipient", phoneNumber)
                "generatePassphrase.sms.accepted"
            } catch (exception: TransientSmsSendException) {
                logger.warn("Passphrase SMS was not accepted by Notify", exception)
                "generatePassphrase.sms.failed"
            } catch (exception: PersistentSmsSendException) {
                logger.warn("Passphrase SMS could not be confirmed", exception)
                "generatePassphrase.sms.unknown"
            }
        redirectAttributes.addFlashAttribute("smsStatus", status)
        redirectAttributes.addFlashAttribute("attemptedPassphrase", passphrase)
        return "redirect:$GENERATE_PASSPHRASE_URL"
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GeneratePassphraseController::class.java)

        const val GENERATE_PASSPHRASE_URL =
            "/$SYSTEM_OPERATOR_PATH_SEGMENT/$GENERATE_PASSPHRASE_PATH_SEGMENT"
    }
}
