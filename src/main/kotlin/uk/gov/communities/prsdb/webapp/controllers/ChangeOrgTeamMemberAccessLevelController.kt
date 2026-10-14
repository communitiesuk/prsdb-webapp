package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.ChangeOrgTeamMemberAccessLevelController.Companion.CHANGE_ACCESS_LEVEL_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.AccessLevelStep
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToOrgLandlordUserService

@PreAuthorize("hasRole('ORG_ADMIN')")
@PrsdbController
@RequestMapping(CHANGE_ACCESS_LEVEL_ROUTE)
class ChangeOrgTeamMemberAccessLevelController(
    private val journeyFactory: ChangeOrgTeamMemberAccessLevelJourneyFactory,
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
    private val userToOrgLandlordUserService: UserToOrgLandlordUserService,
) {
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
        @PathVariable organisationalLandlordUserId: Long,
    ): ModelAndView {
        checkCurrentUserCanEditMember(organisationalLandlordUserId)
        return dispatchJourneyStep(stepPath, organisationalLandlordUserId) { getStepModelAndView() }
    }

    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    @PostMapping("/{*stepPath}")
    fun postJourneyData(
        @PathVariable stepPath: String,
        @PathVariable organisationalLandlordUserId: Long,
        @RequestParam formData: FormData,
    ): ModelAndView {
        checkCurrentUserCanEditMember(organisationalLandlordUserId)
        return dispatchJourneyStep(stepPath, organisationalLandlordUserId) { postStepModelAndView(formData) }
    }

    private fun checkCurrentUserCanEditMember(organisationalLandlordUserId: Long) {
        if (!organisationalLandlordUserService.getCurrentUserIsAuthorizedToEditRecord(organisationalLandlordUserId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No team member with id $organisationalLandlordUserId found in the current organisation",
            )
        }

        val currentMember = userToOrgLandlordUserService.getOrganisationalLandlordUserForCurrentUser()
        if (currentMember?.id == organisationalLandlordUserId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Admins cannot change their own access level")
        }
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        organisationalLandlordUserId: Long,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps(organisationalLandlordUserId) },
            initialiseJourney = { journeyFactory.initializeJourneyState(organisationalLandlordUserId) },
            dispatch = dispatch,
            startNewJourneyOn = { it is PrsdbWebException },
        )

    companion object {
        const val CHANGE_ACCESS_LEVEL_PATH_SEGMENT = "change-access-level"
        const val CHANGE_ACCESS_LEVEL_ROUTE =
            "${TeamMembersController.TEAM_MEMBERS_ROUTE}/{organisationalLandlordUserId}/$CHANGE_ACCESS_LEVEL_PATH_SEGMENT"

        fun getChangeAccessLevelBasePath(organisationalLandlordUserId: Long): String =
            UriTemplate(CHANGE_ACCESS_LEVEL_ROUTE).expand(organisationalLandlordUserId).toASCIIString()

        fun getChangeAccessLevelPath(organisationalLandlordUserId: Long): String =
            "${getChangeAccessLevelBasePath(organisationalLandlordUserId)}/${AccessLevelStep.ROUTE_SEGMENT}"
    }
}
