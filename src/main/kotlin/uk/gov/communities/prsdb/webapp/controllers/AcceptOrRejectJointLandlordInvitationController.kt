package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.ValidateTokenStep
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService

@PrsdbController
@RequestMapping(ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE)
class AcceptOrRejectJointLandlordInvitationController(
    private val journeyFactory: AcceptOrRejectJointLandlordInvitationJourneyFactory,
    private val invitationService: JointLandlordInvitationService,
) {
    @GetMapping
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun startJourney(
        @RequestParam(value = TOKEN, required = true) token: String,
    ): ModelAndView {
        val journeyId = journeyFactory.initializeJourneyState(token)
        // TODO PDJB-260 this url seems to be wrong - it's not including JOINT_LANDLORD_INVITATION_PATH_SEGMENT
        val redirectUrl = JourneyStateService.urlWithJourneyState(ValidateTokenStep.ROUTE_SEGMENT, journeyId)
        invitationService.storeTokenInSession(token)
        return ModelAndView("redirect:$redirectUrl")
    }

    @GetMapping("/{stepRouteSegment}")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun getJourneyStep(
        @PathVariable stepRouteSegment: String,
    ): ModelAndView {
        val token = invitationService.getTokenFromSession() ?: throw(PrsdbWebException("Token not found in session"))
        return try {
            val journeyMap = journeyFactory.createJourneySteps(token)
            journeyMap[stepRouteSegment]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(token, stepRouteSegment)
        }
    }

    @PostMapping("/{stepRouteSegment}")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun postJourneyData(
        @PathVariable stepRouteSegment: String,
        @RequestParam formData: FormData,
    ): ModelAndView {
        val token = invitationService.getTokenFromSession() ?: throw (PrsdbWebException("Token not found in session"))
        return try {
            val journeyMap = journeyFactory.createJourneySteps(token)
            journeyMap[stepRouteSegment]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(token, stepRouteSegment)
        }
    }

    private fun initializeAndRedirect(
        token: String,
        stepRouteSegment: String,
    ): ModelAndView {
        val journeyId = journeyFactory.initializeJourneyState(token)
        val redirectUrl = JourneyStateService.urlWithJourneyState(stepRouteSegment, journeyId)
        return ModelAndView("redirect:$redirectUrl")
    }

    companion object {
        const val ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$JOINT_LANDLORD_INVITATION_PATH_SEGMENT"
    }
}
