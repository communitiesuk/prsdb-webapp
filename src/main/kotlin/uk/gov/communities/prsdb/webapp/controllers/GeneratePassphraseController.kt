package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.GENERATE_PASSPHRASE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.services.PassphraseService

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(GeneratePassphraseController.GENERATE_PASSPHRASE_URL)
class GeneratePassphraseController(
    private val passphraseService: PassphraseService,
) {
    @GetMapping
    fun generatePassphrase(model: Model): String {
        model.addAttribute("passphrase", passphraseService.generatePassphrase())
        return "generatePassphrase"
    }

    companion object {
        const val GENERATE_PASSPHRASE_URL =
            "/$SYSTEM_OPERATOR_PATH_SEGMENT/$GENERATE_PASSPHRASE_PATH_SEGMENT"
    }
}
