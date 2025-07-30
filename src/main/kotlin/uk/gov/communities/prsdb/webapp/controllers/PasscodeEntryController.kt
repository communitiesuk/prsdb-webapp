package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT

@PrsdbController
@RequestMapping("/$LANDLORD_PATH_SEGMENT")
class PasscodeEntryController {
    companion object {
        const val PASSCODE_ENTRY_PATH_SEGMENT = "passcode-entry"
        const val PASSCODE_ENTRY_ROUTE = "/$LANDLORD_PATH_SEGMENT/$PASSCODE_ENTRY_PATH_SEGMENT"
    }

    @GetMapping("/$PASSCODE_ENTRY_PATH_SEGMENT")
    fun passcodeEntry(model: Model): String {
        // TODO: Implement passcode entry functionality
        return "passcodeEntry"
    }
}
