package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AllowIfOrgLandlordUser
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.ValidateTokenStep
import uk.gov.communities.prsdb.webapp.services.OrganisationPermissionsProvider
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@PrsdbController
@RequestMapping(ACCEPT_INVITATION_ROUTE)
class AcceptOrganisationalLandlordUserInvitationController(
    private val journeyFactory: AcceptOrganisationalLandlordUserInvitationJourneyFactory,
    private val invitationService: OrganisationalLandlordInvitationService,
    private val userToLandlordService: UserToLandlordService,
    private val organisationPermissionsProvider: OrganisationPermissionsProvider,
) {
    // TODO PDJB-1828: Remove this annotation when the MULTI_USER_ORGANISATIONS flag is removed
    @GetMapping
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    fun startJourney(
        @RequestParam(value = TOKEN, required = true) token: String,
    ): ModelAndView {
        val journeyId = journeyFactory.initializeJourneyState(token)
        invitationService.addJourneyIdInvitationTokenPairToSession(journeyId, token)
        val startUrl =
            JourneyStateService.urlWithJourneyState(
                "$ACCEPT_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}",
                journeyId,
            )
        return ModelAndView("redirect:$startUrl")
    }

    @GetMapping("/{*stepPath}")
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    fun getJourneyStep(
        @PathVariable stepPath: String,
    ): ModelAndView = dispatchJourneyStep(stepPath) { getStepModelAndView() }

    // TODO PDJB-1828: Remove this annotation when the MULTI_USER_ORGANISATIONS flag is removed
    @PostMapping("/{*stepPath}")
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    fun postJourneyData(
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView = dispatchJourneyStep(stepPath) { postStepModelAndView(formData) }

    // TODO PDJB-1828: Remove this annotation when the MULTI_USER_ORGANISATIONS flag is removed
    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    @AvailableWhenFeatureEnabled(MULTI_USER_ORGANISATIONS)
    @AllowIfOrgLandlordUser
    fun getConfirmation(): ModelAndView {
        val emailAddress =
            invitationService.getAcceptedInvitationEmailFromSession()
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No accepted organisational landlord invitation email was found in the session",
                )

        return ModelAndView(
            "acceptOrganisationInvitationConfirmation",
            mapOf(
                "organisationName" to userToLandlordService.getCurrentOrganisationLandlordForUser().name,
                "emailAddress" to emailAddress,
                "isAdmin" to organisationPermissionsProvider.canCurrentUserPerformOrgAdminActions(),
                "dashboardUrl" to LANDLORD_DASHBOARD_URL,
            ),
        )
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleUninitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps() },
            dispatch = dispatch,
            getRedirect = { ModelAndView("redirect:$ACCEPT_INVITATION_ROUTE") },
        )

    companion object {
        const val ACCEPT_INVITATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/accept-invitation"
        const val ACCEPT_INVITATION_CONFIRMATION_ROUTE = "$ACCEPT_INVITATION_ROUTE/$CONFIRMATION_PATH_SEGMENT"
    }
}
