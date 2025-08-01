package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.SUBMITTED_PASSCODE
import uk.gov.communities.prsdb.webapp.models.requestModels.PasscodeRequestModel

@PrsdbController
@RequestMapping("/$LANDLORD_PATH_SEGMENT")
class PasscodeEntryController {
    companion object {
        const val PASSCODE_ENTRY_PATH_SEGMENT = "passcode-entry"
        const val PASSCODE_ENTRY_ROUTE = "/$LANDLORD_PATH_SEGMENT/$PASSCODE_ENTRY_PATH_SEGMENT"
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

        // Store the passcode in session
        session.setAttribute(SUBMITTED_PASSCODE, passcodeRequestModel.passcode)

        // Redirect to landlord registration index page
        return "redirect:/$LANDLORD_PATH_SEGMENT/$REGISTER_LANDLORD_JOURNEY_URL"
    }
}
