package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CANCEL_JOINT_LANDLORD_INVITATION_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CancelJointLandlordInvitationController.Companion.CANCEL_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.CancelJointLandlordInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import java.security.Principal

@PrsdbController
@PreAuthorize("hasRole('LANDLORD')")
@RequestMapping(CANCEL_JOINT_LANDLORD_INVITATION_ROUTE)
class CancelJointLandlordInvitationController(
    private val journeyFactory: CancelJointLandlordInvitationJourneyFactory,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
) {
    @GetMapping("/{invitationId}/{*stepPath}")
    fun getJourneyStep(
        @PathVariable invitationId: Long,
        @PathVariable stepPath: String,
        principal: Principal,
    ): ModelAndView = dispatchJourneyStep(stepPath, invitationId, principal) { getStepModelAndView() }

    @PostMapping("/{invitationId}/{*stepPath}")
    fun postJourneyData(
        @PathVariable invitationId: Long,
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
        principal: Principal,
    ): ModelAndView = dispatchJourneyStep(stepPath, invitationId, principal) { postStepModelAndView(formData) }

    private fun dispatchJourneyStep(
        stepPath: String,
        invitationId: Long,
        principal: Principal,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps(invitationId, principal.name) },
            initialiseJourney = { journeyFactory.initializeJourneyState() },
            dispatch = dispatch,
        )

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(
        @RequestParam propertyOwnershipId: Long,
    ): ModelAndView {
        val cancelledEmail =
            jointLandlordInvitationService.getCancelledInvitationEmailFromSession()
                ?: throw PrsdbWebException("No joint landlord invitation cancellation found in this session")

        val modelAndView = ModelAndView("cancelJointLandlordInvitationConfirmation")
        modelAndView.addObject("cancelledEmail", cancelledEmail)
        modelAndView.addObject(
            "propertyRecordUrl",
            PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) + "#$LANDLORD_DETAILS_FRAGMENT",
        )
        return modelAndView
    }

    companion object {
        const val CANCEL_JOINT_LANDLORD_INVITATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/$CANCEL_JOINT_LANDLORD_INVITATION_JOURNEY_URL"

        fun getCancelJointLandlordInvitationPath(invitationId: Long): String =
            "$CANCEL_JOINT_LANDLORD_INVITATION_ROUTE/$invitationId/${AreYouSureStep.ROUTE_SEGMENT}"
    }
}
