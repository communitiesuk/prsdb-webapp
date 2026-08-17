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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INDIVIDUAL_LANDLORD_REGISTRATION_SURVEY_URL
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ORG_LANDLORD_REGISTRATION_SURVEY_URL
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.ValidateTokenStep
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@PrsdbController
@RequestMapping(ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE)
class AcceptOrRejectJointLandlordInvitationController(
    private val journeyFactory: AcceptOrRejectJointLandlordInvitationJourneyFactory,
    private val invitationService: JointLandlordInvitationService,
    private val userRolesService: UserRolesService,
    private val userToLandlordService: UserToLandlordService,
) {
    @GetMapping
    fun startJourney(
        @RequestParam(value = TOKEN, required = true) token: String,
    ): ModelAndView {
        val journeyId = journeyFactory.initializeJourneyState(token)
        invitationService.addJourneyIdInvitationTokenPairToSession(journeyId, token)
        val redirectUrl =
            JourneyStateService.urlWithJourneyState(
                "${ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE}/${ValidateTokenStep.ROUTE_SEGMENT}",
                journeyId,
            )
        return ModelAndView("redirect:$redirectUrl")
    }

    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
    ): ModelAndView = dispatchJourneyStep(stepPath) { getStepModelAndView() }

    @PostMapping("/{*stepPath}")
    fun postJourneyData(
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView = dispatchJourneyStep(stepPath) { postStepModelAndView(formData) }

    private fun dispatchJourneyStep(
        stepPath: String,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleUninitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps() },
            dispatch = dispatch,
            getRedirect = { ModelAndView("redirect:$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE") },
        )

    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("/$PROPERTY_JOINED_CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(model: Model): ModelAndView {
        val (propertyAddress, propertyOwnershipId) =
            invitationService.getLastAcceptedPropertyFromSession()
                ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No accepted property details found in the session",
                )

        model.addAttribute("addressParts", propertyAddress.split("\n"))
        model.addAttribute("propertyDetailsUrl", PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId))
        val landlord = userToLandlordService.getCurrentLandlordForUser()
        val surveyUrl =
            if (landlord.landlordType == LandlordType.ORGANISATION) {
                ORG_LANDLORD_REGISTRATION_SURVEY_URL
            } else {
                INDIVIDUAL_LANDLORD_REGISTRATION_SURVEY_URL
            }
        model.addAttribute("landlordRegistrationSurveyUrl", surveyUrl)

        return ModelAndView("acceptJointLandlordInvitationConfirmation")
    }

    @GetMapping("/$INVITATION_REJECTED_PATH_SEGMENT")
    fun getRejectionConfirmation(): ModelAndView {
        val propertyAddress =
            invitationService.getRejectedPropertyAddressFromSession()
                ?: throw PrsdbWebException("No joint landlord invitation rejection data found in this session")

        val modelAndView = ModelAndView("invitationRejectedConfirmation")
        modelAndView.addObject("propertyAddress", propertyAddress)
        return modelAndView
    }

    companion object {
        const val PROPERTY_JOINED_CONFIRMATION_PATH_SEGMENT = "property-joined-$CONFIRMATION_PATH_SEGMENT"

        const val INVITATION_REJECTED_PATH_SEGMENT = "invitation-rejected-$CONFIRMATION_PATH_SEGMENT"

        const val ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$JOINT_LANDLORD_INVITATION_PATH_SEGMENT"

        const val JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE =
            "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/$PROPERTY_JOINED_CONFIRMATION_PATH_SEGMENT"

        const val JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE =
            "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/$INVITATION_REJECTED_PATH_SEGMENT"
    }
}
