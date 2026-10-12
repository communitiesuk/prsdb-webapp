package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.TeamMembersViewModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import java.security.Principal

@PreAuthorize("hasRole('ORG_ADMIN')")
@PrsdbController
@RequestMapping(TEAM_MEMBERS_ROUTE)
class TeamMembersController(
    private val userToLandlordService: UserToLandlordService,
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
) {
    @GetMapping
    // TODO PDJB-1828: Remove this annotation when the MULTI_USER_ORGANISATIONS flag is removed
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    fun getTeamMembers(
        model: Model,
        principal: Principal,
    ): String {
        val landlord = userToLandlordService.getCurrentLandlordForUser()
        if (landlord !is OrganisationalLandlord) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only organisation landlords can view team members")
        }

        val teamMembers =
            TeamMembersViewModel.fromOrganisationalLandlordUsers(
                organisationalLandlordUserService.getOrganisationalLandlordUsers(landlord),
                principal.name,
            )
        model.addAttribute("teamMembers", teamMembers)
        model.addAttribute("administratorsTabId", ADMINISTRATORS_FRAGMENT)
        model.addAttribute("editorsTabId", EDITORS_FRAGMENT)

        return "teamMembers"
    }

    companion object {
        const val TEAM_MEMBERS_PATH_SEGMENT = "team-members"
        const val TEAM_MEMBERS_ROUTE = "/$LANDLORD_PATH_SEGMENT/$TEAM_MEMBERS_PATH_SEGMENT"
        const val ADMINISTRATORS_FRAGMENT = "administrators"
        const val EDITORS_FRAGMENT = "editors"
    }
}
