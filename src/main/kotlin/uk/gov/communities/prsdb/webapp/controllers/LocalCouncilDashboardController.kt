package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.DASHBOARD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.RENTERS_RIGHTS_BILL_PRSD
import uk.gov.communities.prsdb.webapp.controllers.SearchRegisterController.Companion.SEARCH_LANDLORD_URL
import uk.gov.communities.prsdb.webapp.controllers.SearchRegisterController.Companion.SEARCH_PROPERTY_URL
import uk.gov.communities.prsdb.webapp.models.viewModels.NavigationLinkViewModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import java.security.Principal

@PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
@PrsdbController
@RequestMapping("/$LOCAL_COUNCIL_PATH_SEGMENT")
class LocalCouncilDashboardController(
    val localCouncilDataService: LocalCouncilDataService,
    val userRolesService: UserRolesService,
) {
    @GetMapping
    fun index(): CharSequence = "redirect:$LOCAL_AUTHORITY_DASHBOARD_URL"

    @GetMapping("/$DASHBOARD_PATH_SEGMENT")
    fun localAuthorityDashboard(
        model: Model,
        principal: Principal,
    ): String {
        val localAuthorityUser = localCouncilDataService.getLocalAuthorityUser(principal.name)

        val isAdmin = userRolesService.getHasLocalAuthorityAdminRole(principal.name)

        if (isAdmin) {
            model.addAttribute(
                "navLinks",
                listOf(
                    NavigationLinkViewModel(
                        GeneratePasscodeController.GENERATE_PASSCODE_URL,
                        "navLink.generatePasscode.title",
                        false,
                    ),
                    NavigationLinkViewModel(
                        ManageLocalCouncilUsersController.getLaManageUsersRoute(localAuthorityUser.localCouncil.id),
                        "navLink.manageUsers.title",
                        false,
                    ),
                ),
            )
        }

        model.addAttribute("userName", localAuthorityUser.name)
        model.addAttribute("localAuthority", localAuthorityUser.localCouncil.name)
        model.addAttribute("searchPropertyUrl", SEARCH_PROPERTY_URL)
        model.addAttribute("searchLandlordUrl", SEARCH_LANDLORD_URL)
        model.addAttribute("privacyNoticeUrl", LocalCouncilPrivacyNoticeController.LOCAL_COUNCIL_PRIVACY_NOTICE_ROUTE)
        model.addAttribute(
            "rentersRightsBillUrl",
            RENTERS_RIGHTS_BILL_PRSD,
        )
        return "localCouncilDashboard"
    }

    companion object {
        const val LOCAL_AUTHORITY_DASHBOARD_URL = "/$LOCAL_COUNCIL_PATH_SEGMENT/$DASHBOARD_PATH_SEGMENT"
    }
}
