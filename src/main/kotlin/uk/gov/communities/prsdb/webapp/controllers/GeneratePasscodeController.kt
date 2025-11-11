package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.GENERATE_PASSCODE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.exceptions.PasscodeLimitExceededException
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.PasscodeService
import java.security.Principal

@PreAuthorize("hasRole('LA_ADMIN')")
@PrsdbController
@RequestMapping(GeneratePasscodeController.GENERATE_PASSCODE_URL)
@Profile("require-passcode")
class GeneratePasscodeController(
    private val passcodeService: PasscodeService,
    private val localCouncilDataService: LocalCouncilDataService,
) {
    @GetMapping
    fun generatePasscodeGet(
        model: Model,
        principal: Principal,
    ): String {
        val localAuthorityUser = localCouncilDataService.getLocalAuthorityUser(principal.name)
        model.addAttribute("dashboardUrl", LOCAL_AUTHORITY_DASHBOARD_URL)

        return try {
            val passcode = passcodeService.getOrGeneratePasscode(localAuthorityUser.localCouncil.id.toLong())
            model.addAttribute("passcode", passcode)
            "generatePasscode"
        } catch (e: PasscodeLimitExceededException) {
            "error/passcodeLimit"
        }
    }

    @PostMapping
    fun generatePasscodePost(
        model: Model,
        principal: Principal,
    ): String {
        val localAuthorityUser = localCouncilDataService.getLocalAuthorityUser(principal.name)
        model.addAttribute("dashboardUrl", LOCAL_AUTHORITY_DASHBOARD_URL)

        return try {
            val passcode = passcodeService.generateAndStorePasscode(localAuthorityUser.localCouncil.id.toLong())
            model.addAttribute("passcode", passcode)
            model.addAttribute("dashboardUrl", LOCAL_AUTHORITY_DASHBOARD_URL)
            model.addAttribute("backUrl", LOCAL_AUTHORITY_DASHBOARD_URL)
            "generatePasscode"
        } catch (e: PasscodeLimitExceededException) {
            "error/passcodeLimit"
        }
    }

    companion object {
        const val GENERATE_PASSCODE_URL = "/$LOCAL_AUTHORITY_PATH_SEGMENT/$GENERATE_PASSCODE_PATH_SEGMENT"
    }
}
