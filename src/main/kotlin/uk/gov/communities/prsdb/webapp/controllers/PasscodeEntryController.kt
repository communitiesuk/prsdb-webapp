package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.INVALID_PASSCODE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PASSCODE_ENTRY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PASSCODE_REDIRECT_URL
import uk.gov.communities.prsdb.webapp.constants.SUBMITTED_PASSCODE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.models.requestModels.PasscodeRequestModel
import uk.gov.communities.prsdb.webapp.services.PasscodeService

@PrsdbController
@RequestMapping("/$LANDLORD_PATH_SEGMENT")
@Profile("require-passcode")
class PasscodeEntryController(
    private val passcodeService: PasscodeService,
) {
    companion object {
        const val PASSCODE_ENTRY_ROUTE = "/$LANDLORD_PATH_SEGMENT/$PASSCODE_ENTRY_PATH_SEGMENT"
        const val INVALID_PASSCODE_ROUTE = "/$LANDLORD_PATH_SEGMENT/$INVALID_PASSCODE_PATH_SEGMENT"
    }

    @GetMapping("/$PASSCODE_ENTRY_PATH_SEGMENT")
    fun passcodeEntry(model: Model): String {
        model.addAttribute("passcodeRequestModel", PasscodeRequestModel())
        return "passcodeEntry"
    }

    @PostMapping("/$PASSCODE_ENTRY_PATH_SEGMENT", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun submitPasscode(
        model: Model,
        @Valid
        @ModelAttribute
        passcodeRequestModel: PasscodeRequestModel,
        bindingResult: BindingResult,
        session: HttpSession,
    ): String {
        if (bindingResult.hasErrors()) {
            return "passcodeEntry"
        }

        // Validate passcode exists in database
        if (!passcodeService.isValidPasscode(passcodeRequestModel.passcode)) {
            bindingResult.addError(
                FieldError(
                    "passcodeRequestModel",
                    "passcode",
                    "passcodeEntry.error.invalidPasscode",
                ),
            )
            return "passcodeEntry"
        }

        // Store the passcode in session
        session.setAttribute(SUBMITTED_PASSCODE, passcodeRequestModel.passcode)

        // Check for redirect URL in session, otherwise use landlord registration index page
        val redirectUrl = session.getAttribute(PASSCODE_REDIRECT_URL) as? String
        return "redirect:${redirectUrl ?: LANDLORD_REGISTRATION_ROUTE}"
    }

    @GetMapping("/$INVALID_PASSCODE_PATH_SEGMENT")
    fun invalidPasscode(
        model: Model,
        session: HttpSession,
    ): String {
        session.removeAttribute(SUBMITTED_PASSCODE)
        model.addAttribute("passcodeEntryUrl", PASSCODE_ENTRY_ROUTE)
        return "invalidPasscode"
    }
}
