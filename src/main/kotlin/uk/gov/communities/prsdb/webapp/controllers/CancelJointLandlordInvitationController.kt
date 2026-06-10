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
import uk.gov.communities.prsdb.webapp.constants.CANCEL_JOINT_LANDLORD_INVITATION_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CancelJointLandlordInvitationController.Companion.CANCEL_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
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
    @GetMapping("/{invitationId}/{stepName}")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun getJourneyStep(
        @PathVariable invitationId: Long,
        @PathVariable stepName: String,
        principal: Principal,
    ): ModelAndView =
        try {
            journeyFactory.createJourneySteps(invitationId, principal.name)[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(invitationId, stepName)
        }

    @PostMapping("/{invitationId}/{stepName}")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun postJourneyData(
        @PathVariable invitationId: Long,
        @PathVariable stepName: String,
        @RequestParam formData: FormData,
        principal: Principal,
    ): ModelAndView =
        try {
            journeyFactory.createJourneySteps(invitationId, principal.name)[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(invitationId, stepName)
        }

    private fun initializeAndRedirect(
        invitationId: Long,
        stepName: String,
    ): ModelAndView {
        val journeyId = journeyFactory.initializeJourneyState()
        val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
        return ModelAndView("redirect:$CANCEL_JOINT_LANDLORD_INVITATION_ROUTE/$invitationId/$redirectUrl")
    }

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
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
