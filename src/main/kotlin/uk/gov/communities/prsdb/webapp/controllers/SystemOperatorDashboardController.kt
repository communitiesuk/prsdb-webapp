package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.core.env.Environment
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.DASHBOARD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.GeneratePasscodeController.Companion.GENERATE_PASSCODE_URL
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.INVITE_LOCAL_COUNCIL_ADMIN_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.MetricsController.Companion.METRICS_URL

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping("/$SYSTEM_OPERATOR_PATH_SEGMENT")
class SystemOperatorDashboardController(
    private val environment: Environment,
) {
    @GetMapping
    fun index(): CharSequence = "redirect:$SYSTEM_OPERATOR_DASHBOARD_URL"

    @GetMapping("/$DASHBOARD_PATH_SEGMENT")
    fun systemOperatorDashboard(model: Model): String {
        if (environment.activeProfiles.contains("require-passcode")) {
            model.addAttribute("generatePasscodeUrl", GENERATE_PASSCODE_URL)
        }
        model.addAttribute("inviteLocalCouncilAdminUrl", INVITE_LOCAL_COUNCIL_ADMIN_ROUTE)
        model.addAttribute("metricsUrl", METRICS_URL)
        return "systemOperatorDashboard"
    }

    companion object {
        const val SYSTEM_OPERATOR_DASHBOARD_URL = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$DASHBOARD_PATH_SEGMENT"
    }
}
