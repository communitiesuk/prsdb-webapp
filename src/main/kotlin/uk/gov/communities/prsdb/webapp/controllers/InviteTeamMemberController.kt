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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.InviteTeamMemberController.Companion.INVITE_TEAM_MEMBER_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.InviteTeamMemberJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.steps.InviteTeamMemberStep
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@PrsdbController
@PreAuthorize("hasRole('ORG_ADMIN')")
@RequestMapping(INVITE_TEAM_MEMBER_ROUTE)
class InviteTeamMemberController(
    private val journeyFactory: InviteTeamMemberJourneyFactory,
    private val userToLandlordService: UserToLandlordService,
) {
    @GetMapping("/{*stepPath}")
    // TODO PDJB-1828: Remove this annotation when the MULTI_USER_ORGANISATIONS flag is removed
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    fun getJourneyStep(
        @PathVariable stepPath: String,
    ): ModelAndView {
        checkUserIsOrganisationLandlord()
        return dispatchJourneyStep(stepPath) { getStepModelAndView() }
    }

    @PostMapping("/{*stepPath}")
    // TODO PDJB-1828: Remove this annotation when the MULTI_USER_ORGANISATIONS flag is removed
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    fun postJourneyData(
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView {
        checkUserIsOrganisationLandlord()
        return dispatchJourneyStep(stepPath) { postStepModelAndView(formData) }
    }

    private fun checkUserIsOrganisationLandlord() {
        if (userToLandlordService.getCurrentLandlordForUser() !is OrganisationalLandlord) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only organisation landlords can invite team members")
        }
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps() },
            initialiseJourney = { journeyFactory.initializeJourneyState() },
            dispatch = dispatch,
        )

    companion object {
        const val INVITE_TEAM_MEMBER_PATH_SEGMENT = "invite"
        const val INVITE_TEAM_MEMBER_ROUTE = "$TEAM_MEMBERS_ROUTE/$INVITE_TEAM_MEMBER_PATH_SEGMENT"
        const val INVITE_TEAM_MEMBER_START_PATH = "$INVITE_TEAM_MEMBER_ROUTE/${InviteTeamMemberStep.ROUTE_SEGMENT}"
    }
}
