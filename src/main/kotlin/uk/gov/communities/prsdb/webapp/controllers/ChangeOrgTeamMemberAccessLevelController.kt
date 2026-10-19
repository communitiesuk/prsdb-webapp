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
import uk.gov.communities.prsdb.webapp.exceptions.OrganisationalLandlordUserMismatchException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.AccessLevelStep
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.RemoveOrgUserConfirmationStep
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService

@PreAuthorize("hasRole('ORG_ADMIN')")
@PrsdbController
@RequestMapping(CHANGE_ACCESS_LEVEL_ROUTE)
class ChangeOrgTeamMemberAccessLevelController(
    private val journeyFactory: ChangeOrgTeamMemberAccessLevelJourneyFactory,
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
) {
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
        @PathVariable otherOrganisationalLandlordUserId: Long,
    ): ModelAndView {
        checkCurrentUserCanEditMember(otherOrganisationalLandlordUserId)
        return dispatchJourneyStep(stepPath, otherOrganisationalLandlordUserId) { getStepModelAndView() }
    }

    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    @PostMapping("/{*stepPath}")
    fun postJourneyData(
        @PathVariable stepPath: String,
        @PathVariable otherOrganisationalLandlordUserId: Long,
        @RequestParam formData: FormData,
    ): ModelAndView {
        checkCurrentUserCanEditMember(otherOrganisationalLandlordUserId)
        return dispatchJourneyStep(stepPath, otherOrganisationalLandlordUserId) { postStepModelAndView(formData) }
    }

    private fun checkCurrentUserCanEditMember(otherOrganisationalLandlordUserId: Long) {
        if (!organisationalLandlordUserService.getCurrentUserIsAuthorizedToEditRecordOfOtherUser(otherOrganisationalLandlordUserId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No org team member with id $otherOrganisationalLandlordUserId that the user is authorised to edit found",
            )
        }
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        otherOrganisationalLandlordUserId: Long,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps(otherOrganisationalLandlordUserId) },
            initialiseJourney = { journeyFactory.initializeJourneyState(otherOrganisationalLandlordUserId) },
            dispatch = dispatch,
            startNewJourneyOn = { it is OrganisationalLandlordUserMismatchException },
        )

    companion object {
        const val CHANGE_ACCESS_LEVEL_PATH_SEGMENT = "change-access-level"
        const val CHANGE_ACCESS_LEVEL_ROUTE =
            "${TeamMembersController.TEAM_MEMBERS_ROUTE}/{otherOrganisationalLandlordUserId}/$CHANGE_ACCESS_LEVEL_PATH_SEGMENT"

        fun getChangeAccessLevelBasePath(otherOrganisationalLandlordUserId: Long): String =
            UriTemplate(CHANGE_ACCESS_LEVEL_ROUTE).expand(otherOrganisationalLandlordUserId).toASCIIString()

        fun getChangeAccessLevelPath(otherOrganisationalLandlordUserId: Long): String =
            "${getChangeAccessLevelBasePath(otherOrganisationalLandlordUserId)}/${AccessLevelStep.ROUTE_SEGMENT}"

        fun getRemoveOrgUserPath(otherOrganisationalLandlordUserId: Long): String =
            "${getChangeAccessLevelBasePath(otherOrganisationalLandlordUserId)}/${RemoveOrgUserConfirmationStep.ROUTE_SEGMENT}"
    }
}
