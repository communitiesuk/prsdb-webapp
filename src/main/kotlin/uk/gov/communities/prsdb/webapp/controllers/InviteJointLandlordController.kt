package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.RESEND_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.InviteJointLandlordController.Companion.INVITE_JOINT_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord.InviteJointLandlordJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.StartInviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbController
@RequestMapping(INVITE_JOINT_LANDLORD_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class InviteJointLandlordController(
    private val journeyFactory: InviteJointLandlordJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val landlordService: LandlordService,
) {
    @GetMapping("{stepName}")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun getUpdateStep(
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        return try {
            val journeyMap = journeyFactory.createJourneySteps(propertyOwnershipId)
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = journeyFactory.initializeJourneyState(propertyOwnershipId, principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
    }

    @PostMapping("{stepName}")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun postUpdateStep(
        model: Model,
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: FormData,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        return try {
            val journeyMap = journeyFactory.createJourneySteps(propertyOwnershipId)
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = journeyFactory.initializeJourneyState(propertyOwnershipId, principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
    }

    // TODO: PDJB-1060: We should not be using a GET for editing actions. Replace with a confirmation page.
    @GetMapping("$RESEND_PATH_SEGMENT/{invitationId}")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun resendInvitation(
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable invitationId: Long,
        redirectAttributes: RedirectAttributes,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        val invitingLandlord =
            landlordService.retrieveLandlordByBaseUserId(principal.name)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Landlord not found for user ${principal.name}")
        val email = jointLandlordInvitationService.resendInvitation(invitationId, propertyOwnership, invitingLandlord)
        redirectAttributes.addFlashAttribute("resendInvitationEmail", email)
        return "redirect:${PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId)}#$LANDLORD_DETAILS_FRAGMENT"
    }

    @GetMapping(CONFIRMATION_PATH_SEGMENT)
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun getConfirmation(
        model: Model,
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        model.addAttribute(
            "propertyDetailsUrl",
            PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) + "#$LANDLORD_DETAILS_FRAGMENT",
        )
        return "inviteJointLandlordConfirmation"
    }

    private fun throwErrorIfUserIsNotAuthorized(
        baseUserId: String,
        propertyOwnershipId: Long,
    ) {
        if (!propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, baseUserId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User $baseUserId is not authorized to update property ownership $propertyOwnershipId",
            )
        }
    }

    companion object {
        const val INVITE_JOINT_LANDLORD_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{propertyOwnershipId}/invite-joint-landlord"

        fun getInviteJointLandlordRoute(propertyOwnershipId: Long): String =
            INVITE_JOINT_LANDLORD_ROUTE.replace("{propertyOwnershipId}", propertyOwnershipId.toString())

        fun getInviteJointLandlordFirstStepPath(propertyOwnershipId: Long): String =
            "${getInviteJointLandlordRoute(propertyOwnershipId)}/${StartInviteJointLandlordStep.ROUTE_SEGMENT}"

        fun getResendInvitationPath(
            propertyOwnershipId: Long,
            invitationId: Long,
        ): String = "${getInviteJointLandlordRoute(propertyOwnershipId)}/$RESEND_PATH_SEGMENT/$invitationId"
    }
}
