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
import uk.gov.communities.prsdb.webapp.controllers.InviteOrganisationalLandlordUserController.Companion.INVITE_ORGANISATIONAL_LANDLORD_USER_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.InviteOrganisationalLandlordUserJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.steps.InviteOrganisationalLandlordUserStep
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@PrsdbController
@PreAuthorize("hasRole('ORG_ADMIN')")
@RequestMapping(INVITE_ORGANISATIONAL_LANDLORD_USER_ROUTE)
class InviteOrganisationalLandlordUserController(
    private val journeyFactory: InviteOrganisationalLandlordUserJourneyFactory,
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
        const val INVITE_ORGANISATIONAL_LANDLORD_USER_PATH_SEGMENT = "invite"
        const val INVITE_ORGANISATIONAL_LANDLORD_USER_ROUTE = "$TEAM_MEMBERS_ROUTE/$INVITE_ORGANISATIONAL_LANDLORD_USER_PATH_SEGMENT"
        const val INVITE_ORGANISATIONAL_LANDLORD_USER_PATH =
            "$INVITE_ORGANISATIONAL_LANDLORD_USER_ROUTE/${InviteOrganisationalLandlordUserStep.ROUTE_SEGMENT}"
    }
}
