package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.DASHBOARD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import java.security.Principal

@PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
@Controller
@RequestMapping("/$LOCAL_AUTHORITY_PATH_SEGMENT")
class LocalAuthorityDashboardController(
    val localAuthorityDataService: LocalAuthorityDataService,
) {
    @GetMapping
    fun index(model: Model): String = "redirect:$LOCAL_AUTHORITY_DASHBOARD_URL"

    @GetMapping("/$DASHBOARD_PATH_SEGMENT")
    fun localAuthorityDashboard(
        model: Model,
        principal: Principal,
    ): String {
        val localAuthorityUser = localAuthorityDataService.getLocalAuthorityUser(principal.name)

        model.addAttribute("title", "dashboard.title")
        model.addAttribute("userName", localAuthorityUser.name)
        model.addAttribute("localAuthority", localAuthorityUser.localAuthority.name)

        return "localAuthorityDashboard"
    }

    companion object {
        const val LOCAL_AUTHORITY_DASHBOARD_URL = "/$LOCAL_AUTHORITY_PATH_SEGMENT/$DASHBOARD_PATH_SEGMENT"
    }
}
