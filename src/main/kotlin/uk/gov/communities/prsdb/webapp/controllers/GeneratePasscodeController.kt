package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.GENERATE_PASSCODE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.exceptions.PasscodeLimitExceededException
import uk.gov.communities.prsdb.webapp.services.PasscodeService

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(GeneratePasscodeController.GENERATE_PASSCODE_URL)
@Profile("require-passcode")
class GeneratePasscodeController(
    private val passcodeService: PasscodeService,
) {
    @GetMapping
    fun generatePasscodeGet(model: Model): String {
        return try {
            val passcode = passcodeService.getOrGeneratePasscode()
            model.addAttribute("passcode", passcode)
            "generatePasscode"
        } catch (e: PasscodeLimitExceededException) {
            "error/passcodeLimit"
        }
    }

    @PostMapping
    fun generatePasscodePost(model: Model): String {
        return try {
            val passcode = passcodeService.generateAndStorePasscode()
            model.addAttribute("passcode", passcode)
            "generatePasscode"
        } catch (e: PasscodeLimitExceededException) {
            "error/passcodeLimit"
        }
    }

    companion object {
        const val GENERATE_PASSCODE_URL = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$GENERATE_PASSCODE_PATH_SEGMENT"
    }
}
