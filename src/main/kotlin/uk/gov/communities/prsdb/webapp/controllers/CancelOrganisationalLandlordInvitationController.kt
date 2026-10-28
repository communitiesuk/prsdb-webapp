package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.CancelOrganisationalLandlordInvitationController.Companion.CANCEL_ORGANISATIONAL_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.CancelOrganisationalLandlordInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.stepConfig.AreYouSureStep

@PreAuthorize("hasRole('ORG_ADMIN')")
@PrsdbController
@RequestMapping(CANCEL_ORGANISATIONAL_LANDLORD_INVITATION_ROUTE)
class CancelOrganisationalLandlordInvitationController(
    private val journeyFactory: CancelOrganisationalLandlordInvitationJourneyFactory,
) {
    // TODO PDJB-1828: Remove this annotation when the MULTI_USER_ORGANISATIONS flag is removed
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    @GetMapping("/{invitationId}/{*stepPath}")
    fun getJourneyStep(
        @PathVariable invitationId: Long,
        @PathVariable stepPath: String,
    ): ModelAndView = dispatchJourneyStep(stepPath, invitationId) { getStepModelAndView() }

    // TODO PDJB-1828: Remove this annotation when the MULTI_USER_ORGANISATIONS flag is removed
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    @PostMapping("/{invitationId}/{*stepPath}")
    fun postJourneyData(
        @PathVariable invitationId: Long,
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView = dispatchJourneyStep(stepPath, invitationId) { postStepModelAndView(formData) }

    private fun dispatchJourneyStep(
        stepPath: String,
        invitationId: Long,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps(invitationId) },
            initialiseJourney = { journeyFactory.initializeJourneyState() },
            dispatch = dispatch,
        )

    companion object {
        const val CANCEL_ORGANISATIONAL_LANDLORD_INVITATION_ROUTE = "$TEAM_MEMBERS_ROUTE/$CANCEL_INVITATION_PATH_SEGMENT"

        fun getCancelInvitationPath(invitationId: Long): String =
            "$CANCEL_ORGANISATIONAL_LANDLORD_INVITATION_ROUTE/$invitationId/${AreYouSureStep.ROUTE_SEGMENT}"
    }
}
